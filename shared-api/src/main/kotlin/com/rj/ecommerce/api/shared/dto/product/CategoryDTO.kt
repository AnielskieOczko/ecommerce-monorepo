package com.rj.ecommerce.api.shared.dto.product

/**
 * Represents a product category.
 *
 * @property id Category ID.
 * @property name Category name.
 * @property description Optional description of the category.
 *
 * Requirements:
 * - id and name are required
 * - description is optional
 */
data class CategoryDTO(
    val id: Long,
    val name: String,
    val description: String? = null
)
