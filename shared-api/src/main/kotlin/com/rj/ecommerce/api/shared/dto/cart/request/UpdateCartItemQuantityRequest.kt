package com.rj.ecommerce.api.shared.dto.cart.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min

@Schema(description = "Request to update the quantity of an existing item in the cart.")
data class UpdateCartItemQuantityRequest(
    @field:Schema(description = "The new quantity for the cart item. Use 0 to remove the item from the cart.", required = true, example = "3")
    @field:Min(0, message = "New quantity cannot be negative. Use 0 to remove.")
    val newQuantity: Int
)