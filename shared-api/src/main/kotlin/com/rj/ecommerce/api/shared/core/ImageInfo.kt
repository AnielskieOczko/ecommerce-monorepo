package com.rj.ecommerce.api.shared.core

/**
 * Represents metadata for an image.
 *
 * @property id Internal DB ID, may not be relevant for all contexts.
 * @property path URL or path to the image.
 * @property altText Alternative text for accessibility.
 * @property mimeType MIME type of the image (e.g., "image/jpeg").
 *
 * Requirements:
 * - path, altText, and mimeType are required
 * - id is optional and typically auto-generated
 */
data class ImageInfo(
    val id: Long? = null,
    val path: String,
    val altText: String,
    val mimeType: String
)
