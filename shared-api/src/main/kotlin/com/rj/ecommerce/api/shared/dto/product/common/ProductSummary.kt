package com.rj.ecommerce.api.shared.dto.product.common

import com.rj.ecommerce.api.shared.core.Money
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A concise summary of a product, used for listings and nested representations.")
data class ProductSummary(
    @field:Schema(description = "The unique ID of the product.", example = "101")
    val id: Long,

    @field:Schema(description = "The name of the product.", example = "Premium Wireless Keyboard")
    val name: String?,

    @field:Schema(description = "The price per unit of the product.")
    val unitPrice: Money?
)