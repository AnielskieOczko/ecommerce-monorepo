package com.rj.ecommerce.api.shared.dto.cart

import jakarta.validation.constraints.Min

/**
 * Request to add an item to a cart.
 *
 * @property productId ID of the product to add to the cart.
 * @property quantity Number of units to add to the cart.
 *
 * Requirements:
 * - productId and quantity are required
 * - quantity must be at least 1
 */
data class AddItemToCartRequestDTO(
    val productId: Long,
    @field:Min(1, message = "Quantity must be at least 1.")
    val quantity: Int = 1
) {
    init {
        require(quantity >= 1) { "Quantity must be at least 1" }
    }
}
