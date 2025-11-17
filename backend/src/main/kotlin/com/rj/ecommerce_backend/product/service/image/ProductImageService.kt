package com.rj.ecommerce_backend.product.service.image

import com.rj.ecommerce_backend.api.shared.core.ImageInfo
import com.rj.ecommerce_backend.product.domain.Product
import org.springframework.web.multipart.MultipartFile

/**
 * A dedicated service for managing all aspects of product images,
 * including file storage, entity persistence, and metadata.
 */
interface ProductImageService {
    /**
     * Attaches a list of new images to an existing product, stores the files,
     * and returns the DTOs for the newly created images.
     *
     * @param productId The ID of the product to add images to.
     * @param images The list of image files to upload.
     * @param altText An optional, single alt text to apply to all uploaded images. If null, filenames are used.
     * @return A list of ImageInfo DTOs for the newly added images.
     */
    fun addImagesToProduct(productId: Long, images: List<MultipartFile>, altText: String?): List<ImageInfo>

    /**
     * Updates the metadata (e.g., alt text) of a single, existing image.
     *
     * @param productId The ID of the product the image belongs to (for security verification).
     * @param imageId The ID of the image to update.
     * @param altText The new alternative text for the image.
     * @return The updated ImageInfo DTO.
     */
    fun updateImageMetadata(productId: Long, imageId: Long, altText: String): ImageInfo

    /**
     * Deletes an image from a product, its database record, and its physical file(s) from storage.
     *
     * @param productId The ID of the product the image belongs to.
     * @param imageId The ID of the image to delete.
     */
    fun deleteImage(productId: Long, imageId: Long)

    /**
     * A helper method for other services to get fully-formed ImageInfo DTOs (with public URLs)
     * for a given Product entity. This is intended for internal use by other query services.
     *
     * @param product The Product entity whose images are to be mapped.
     * @return A list of ImageInfo DTOs, complete with public URLs.
     */
    fun getImageInfosForProduct(product: Product): List<ImageInfo>


    fun deleteAllImagesForProduct(product: Product)
}