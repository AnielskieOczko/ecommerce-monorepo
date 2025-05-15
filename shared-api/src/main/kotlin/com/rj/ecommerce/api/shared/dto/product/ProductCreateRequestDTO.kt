package com.rj.ecommerce.api.shared.dto.product

import com.rj.ecommerce.api.shared.core.ImageInfo
import com.rj.ecommerce.api.shared.core.Money

/**
 * Request to create a new product.
 *
 * @property name Product name.
 * @property description Detailed description of the product.
 * @property unitPrice Price per unit of the product.
 * @property quantityInStock Number of units available in stock.
 * @property categoryIds IDs of categories this product belongs to.
 * @property images List of images associated with this product.
 *
 * Requirements:
 * - name, description, unitPrice, and quantityInStock are required
 * - categoryIds and images are optional
 */
data class ProductCreateRequestDTO(
    val name: String,
    val description: String,
    val unitPrice: Money,
    val quantityInStock: Int,
    val categoryIds: List<Long> = emptyList(),
    val images: List<ImageInfo> = emptyList()
)
