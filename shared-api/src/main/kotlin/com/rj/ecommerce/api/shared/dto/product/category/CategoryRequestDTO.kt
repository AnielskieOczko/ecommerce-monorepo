package com.rj.ecommerce.api.shared.dto.product.category

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Request model for creating or updating a product category.")
data class CategoryRequestDTO(
    @field:Schema(description = "The name of the category. Must be unique.", example = "Laptops")
    @field:NotBlank(message = "Category name cannot be blank.")
    val name: String
)