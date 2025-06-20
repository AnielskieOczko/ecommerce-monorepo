package com.rj.ecommerce.api.shared.dto.product

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.rj.ecommerce.api.shared.core.ImageInfo
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Min

@Schema(description = "Request to create a new product.")
data class ProductCreateRequestDTO(
    @field:Valid
    @field:JsonUnwrapped
    val productData: ProductBase,

    @field:Schema(description = "Number of units available in stock.", example = "150")
    @field:Min(0)
    val quantityInStock: Int,

    @field:Schema(description = "List of images associated with this product.")
    val images: List<ImageInfo> = emptyList()
)
