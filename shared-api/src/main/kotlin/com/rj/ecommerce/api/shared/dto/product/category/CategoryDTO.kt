package com.rj.ecommerce.api.shared.dto.product.category

/**
 * Represents a product category.
 *
 * @property id Category ID.
 * @property name Category name.
 *
 * Requirements:
 * - id and name are required
 * - description is optional
 */
data class CategoryDTO(
    val id: Long?,
    val name: String,
)
