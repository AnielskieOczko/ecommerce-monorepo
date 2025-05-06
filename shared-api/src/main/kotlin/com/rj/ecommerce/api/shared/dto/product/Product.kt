package com.rj.ecommerce.api.shared.dto.product

import com.rj.ecommerce.api.shared.core.ImageInfo
import com.rj.ecommerce.api.shared.core.Money

/**
 * Represents a product returned by the API.
 *
 * @property id Product ID (Using String for cross-service consistency).
 * @property sku Stock Keeping Unit, if applicable.
 * @property name Product name.
 * @property unitPrice Price per unit of the product.
 * @property description Detailed description of the product.
 * @property quantityInStock Number of units available in stock.
 * @property categories List of categories this product belongs to.
 * @property images List of images associated with this product.
 *
 * Requirements:
 * - id, name, unitPrice, description, and quantityInStock are required
 * - sku, categories, and images are optional
 */
data class Product(
    val id: String,
    val sku: String? = null,
    val name: String,
    val unitPrice: Money,
    val description: String,
    val quantityInStock: Int,
    val categories: List<Category> = emptyList(),
    val images: List<ImageInfo> = emptyList()
)
