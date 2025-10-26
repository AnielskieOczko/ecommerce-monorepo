package com.rj.ecommerce_backend.api.shared.dto.cart.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min

@Schema(description = "Request to add a specific product to the shopping cart.")
data class AddItemToCartRequest(
    @field:Schema(description = "The unique ID of the product to add.", required = true, example = "101")
    val productId: Long,

    @field:Schema(description = "The number of units to add. Must be at least 1.", required = true, example = "2", defaultValue = "1")
    @field:Min(1, message = "Quantity must be at least 1.")
    val quantity: Int = 1
)
