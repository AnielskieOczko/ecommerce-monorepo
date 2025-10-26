package com.rj.ecommerce_backend.api.shared.dto.product.common

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Represents a product category. Used for both requests and responses.")
data class CategoryDetails(
    @field:Schema(description = "The unique identifier of the category.", example = "12")
    val id: Long?,

    @field:Schema(description = "The name of the category. Must be unique.", example = "Laptops", required = true)
    @field:NotBlank(message = "Category name cannot be blank.")
    val name: String
)
