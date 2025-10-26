// File: backend/src/main/kotlin/com/rj/ecommerce_backend/product/service/ProductImageServiceImpl.kt
package com.rj.ecommerce_backend.product.service.image

import com.rj.ecommerce.api.shared.core.ImageInfo
import com.rj.ecommerce_backend.product.domain.Image
import com.rj.ecommerce_backend.product.domain.Product
import com.rj.ecommerce_backend.product.exception.FileStorageException
import com.rj.ecommerce_backend.product.exception.ImageNotFoundException
import com.rj.ecommerce_backend.product.exception.ProductNotFoundException
import com.rj.ecommerce_backend.product.repository.ImageRepository
import com.rj.ecommerce_backend.product.repository.ProductRepository
import com.rj.ecommerce_backend.storage.service.StorageService
import io.github.oshai.kotlinlogging.KotlinLogging
import net.coobird.thumbnailator.Thumbnails
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

private val log = KotlinLogging.logger {}

@Service
@Transactional
class ProductImageServiceImpl(
    private val storageService: StorageService,
    private val imageRepository: ImageRepository,
    private val productRepository: ProductRepository
) : ProductImageService {

    override fun addImagesToProduct(productId: Long, images: List<MultipartFile>, altText: String?): List<ImageInfo> {
        val product = productRepository.findById(productId)
            .orElseThrow { ProductNotFoundException(productId) }

        images.filterNot { it.isEmpty }.forEachIndexed { index, file ->
            val resolvedAltText = altText ?: file.originalFilename ?: "Product ${product.name.value} Image ${index + 1}"
            assignImageToProduct(file, resolvedAltText, product)
        }

        // The assignImageToProduct helper adds to the collection, now we save the parent to cascade the changes.
        val savedProduct = productRepository.save(product)
        return getImageInfosForProduct(savedProduct)
    }

    override fun updateImageMetadata(productId: Long, imageId: Long, altText: String): ImageInfo {
        val image = findImageOnProduct(productId, imageId)
        image.altText = altText
        val savedImage = imageRepository.save(image)
        return mapToImageInfo(savedImage)
    }

    override fun deleteImage(productId: Long, imageId: Long) {
        log.info { "Request to delete image ID: $imageId from product ID: $productId" }
        val product = productRepository.findById(productId)
            .orElseThrow { ProductNotFoundException(productId) }

        val imageToDelete = product.images.find { it.id == imageId }
            ?: throw ImageNotFoundException("Image with ID $imageId not found on product $productId")

        // 1. Remove the image from the product's collection in memory.
        product.removeImage(imageToDelete)

        // 2. Save the product. `orphanRemoval=true` on the @OneToMany relationship
        //    will trigger Hibernate to DELETE the image from the `images` table.
        productRepository.save(product)

        // 3. After the transaction commits, delete the physical files.
        // This is done last to avoid having orphaned DB records if file deletion fails.
        try {
            storageService.delete(imageToDelete.fileIdentifier)
            imageToDelete.webpFileIdentifier?.let { storageService.delete(it) }
        } catch (e: Exception) {
            log.error(e) { "Failed to delete physical files for already-deleted image entity ${imageToDelete.id}. Manual cleanup may be required." }
        }
    }

    override fun getImageInfosForProduct(product: Product): List<ImageInfo> {
        return product.images.map { mapToImageInfo(it) }
    }

    override fun deleteAllImagesForProduct(product: Product) {
        log.info { "Deleting all images for product ID: ${product.id}" }
        // Create a copy of the list to avoid ConcurrentModificationException while iterating
        val imagesToDelete = product.images.toList()
        imagesToDelete.forEach { image ->
            // The existing deleteImage logic is complex, so we just call it.
            // A more optimized version could delete all files first, then do one DB update.
            // But this is safer and reuses existing logic.
            deleteImage(product.id!!, image.id!!)
        }
    }

    /**
     * Internal helper to handle the logic of storing a file and creating an Image entity.
     * It adds the new Image to the Product's collection but does not save the product itself.
     */
    private fun assignImageToProduct(file: MultipartFile, altText: String, product: Product) {
        try {
            val fileIdentifier = storageService.store(file)
            val image = Image(
                fileIdentifier = fileIdentifier,
                altText = altText,
                mimeType = file.contentType ?: "application/octet-stream",
                product = product
            )
            product.addImage(image)

            // Note: The product must be saved *after* this method call for the image to be persisted.
            // We need to save once to get an ID for the async task.
            productRepository.flush() // Flush to ensure the image is assigned an ID before the async call
            val newImage = product.images.find { it.fileIdentifier == fileIdentifier }
                ?: throw IllegalStateException("Could not find newly added image in product collection.")

            this.optimizeAndCreateWebp(newImage.id!!, fileIdentifier)

        } catch (e: FileStorageException) {
            log.error(e) { "Failed to store image ${file.originalFilename} for product ${product.id}" }
            // Decide on error strategy: re-throw to fail the whole transaction or just log and skip.
            // For now, we log and continue.
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun optimizeAndCreateWebp(imageId: Long, originalFileIdentifier: String) {
        val webpIdentifier = "${StringUtils.stripFilenameExtension(originalFileIdentifier)}.webp"
        try {
            val resource = storageService.loadAsResource(originalFileIdentifier)
            val outputStream = ByteArrayOutputStream()

            resource.inputStream.use { inputStream ->
                Thumbnails.of(inputStream)
                    .size(1024, 1024) // Or another appropriate size
                    .outputFormat("webp")
                    .outputQuality(0.85)
                    .toOutputStream(outputStream)
            }

            val optimizedInputStream = ByteArrayInputStream(outputStream.toByteArray())
            storageService.store(optimizedInputStream, webpIdentifier)

            val imageToUpdate = imageRepository.findById(imageId)
                .orElseThrow { IllegalStateException("Image with ID $imageId not found for WebP update.") }

            imageToUpdate.webpFileIdentifier = webpIdentifier
            imageRepository.save(imageToUpdate)

            log.info { "Successfully created and linked optimized WebP version: $webpIdentifier for Image ID: $imageId" }
        } catch (e: Exception) {
            log.error(e) { "Failed to create optimized WebP version for Image ID: $imageId" }
        }
    }

    private fun findImageOnProduct(productId: Long, imageId: Long): Image {
        val image = imageRepository.findById(imageId).orElseThrow { ImageNotFoundException("Image with ID $imageId not found.") }
        if (image.product.id != productId) {
            throw SecurityException("Image $imageId does not belong to product $productId.")
        }
        return image
    }

    private fun mapToImageInfo(image: Image): ImageInfo {
        // Prefer the optimized WebP version if it exists, otherwise fall back to the original.
        val identifier = image.webpFileIdentifier ?: image.fileIdentifier
        val mimeType = if (image.webpFileIdentifier != null) "image/webp" else image.mimeType

        return ImageInfo(
            id = image.id,
            path = storageService.getPublicUrl(identifier),
            altText = image.altText,
            mimeType = mimeType
        )
    }
}