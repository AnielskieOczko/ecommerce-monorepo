package com.rj.ecommerce.api.shared.dto.cart

import jakarta.validation.constraints.Min

data class UpdateCartItemQuantityRequestDTO(
    @field:Min(0, message = "New quantity cannot be negative. Use 0 to remove.")
    val newQuantity: Int
)