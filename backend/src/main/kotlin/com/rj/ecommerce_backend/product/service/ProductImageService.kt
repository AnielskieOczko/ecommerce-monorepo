package com.rj.ecommerce_backend.product.service

import com.rj.ecommerce_backend.product.domain.Image
import com.rj.ecommerce_backend.product.domain.Product
import com.rj.ecommerce_backend.product.exception.FileStorageException
import com.rj.ecommerce_backend.product.repository.ImageRepository
import com.rj.ecommerce_backend.storage.service.StorageService
import io.github.oshai.kotlinlogging.KotlinLogging
import net.coobird.thumbnailator.Thumbnails
import org.springframework.core.io.Resource
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

private val logger = KotlinLogging.logger { ProductImageService::class }

@Service
class ProductImageService(
    private val storageService: StorageService,
    private val imageRepository: ImageRepository
) {

    @Transactional
    fun assignImageToProduct(file: MultipartFile, altText: String, product: Product): Image {
        if (file.isEmpty) {
            throw FileStorageException("Cannot store an empty file.")
        }
        // 1. Delegate storing the physical file to the storage service.
        val fileIdentifier = storageService.store(file)

        // 2. The rest is database logic.
        val image = Image(
            fileIdentifier = fileIdentifier,
            altText = altText.ifBlank { "Product image" },
            mimeType = file.contentType ?: "application/octet-stream",
            product = product
        )

        val savedImage = imageRepository.save(image)

        this.optimizeAndCreateWebp(savedImage.id!!, fileIdentifier)
        return savedImage
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
                    .size(1024, 1024)
                    .outputFormat("webp")
                    .outputQuality(0.85)
                    .toOutputStream(outputStream)
            }

            val optimizedInputStream = ByteArrayInputStream(outputStream.toByteArray())
            val storedWebpIdentifier = storageService.store(optimizedInputStream, webpIdentifier)

            // --- THE MANDATORY DATABASE UPDATE ---
            val imageToUpdate = imageRepository.findById(imageId)
                .orElseThrow { IllegalStateException("Image with ID $imageId not found for WebP update.") }

            imageToUpdate.webpFileIdentifier = storedWebpIdentifier
            imageRepository.save(imageToUpdate)

            logger.info { "Successfully created and linked optimized version: $storedWebpIdentifier for Image ID: $imageId" }

        } catch (e: Exception) {
            // If anything fails, we just log it. The original image still exists and is usable.
            logger.error(e) { "Failed to create optimized version for Image ID: $imageId" }
        }
    }

    @Transactional
    fun deleteImage(image: Image) {
        // To prevent orphan files, delete the DB record first.
        val identifier = image.fileIdentifier
        imageRepository.delete(image)

        // Then, attempt to delete the physical file.
        // If this fails, we log it but don't fail the transaction.
        // An orphaned file is better than an orphaned DB record.
        try {
            storageService.delete(identifier)
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete physical file $identifier for already-deleted image entity ${image.id}. Manual cleanup may be required." }
        }
    }

    // This method might now live in a different controller/service that just serves data
    fun getResourceForImage(image: Image): Resource {
        return storageService.loadAsResource(image.fileIdentifier)
    }


}