package com.rj.ecommerce.api.shared.dto.product

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.rj.ecommerce.api.shared.core.ImageInfo
import com.rj.ecommerce.api.shared.dto.product.category.CategoryDTO
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Detailed representation of a product for API responses.")
data class ProductResponseDTO(
    @field:Schema(description = "The unique identifier of the product.", example = "101")
    val id: Long,

    @field:JsonUnwrapped
    val productData: ProductBase,

    @field:Schema(description = "The number of units currently in stock.", example = "250")
    val quantity: Int,

    @field:Schema(description = "A list of fully-hydrated categories the product belongs to.")
    val categories: List<CategoryDTO>,

    @field:Schema(description = "A list of images associated with the product.")
    val images: List<ImageInfo>
)