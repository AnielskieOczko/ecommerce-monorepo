package com.rj.ecommerce.api.shared.dto.product

import com.rj.ecommerce.api.shared.core.ImageInfo
import com.rj.ecommerce.api.shared.core.Money

/**
 * Request to update an existing product.
 *
 * @property name Updated product name.
 * @property description Updated product description.
 * @property unitPrice Updated product price.
 * @property quantityInStock Updated product stock quantity.
 * @property categoryIds Updated list of category IDs.
 * @property images Updated list of product images.
 *
 * Requirements:
 * - All fields are optional, allowing partial updates
 */
data class ProductUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val unitPrice: Money? = null,
    val quantityInStock: Int? = null,
    val categoryIds: List<Long>? = null,
    val images: List<ImageInfo>? = null
)
