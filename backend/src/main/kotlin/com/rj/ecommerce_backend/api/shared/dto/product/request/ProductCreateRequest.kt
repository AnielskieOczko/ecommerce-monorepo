package com.rj.ecommerce_backend.api.shared.dto.product.request

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.rj.ecommerce.api.shared.dto.product.common.ProductBase
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Min

@Schema(description = "Request to create a new product.")
data class ProductCreateRequest(
    @field:Valid
    @field:JsonUnwrapped
    val productData: ProductBase,

    @field:Schema(description = "Number of units available in stock.", example = "150", required = true)
    @field:Min(0)
    val quantityInStock: Int
)
