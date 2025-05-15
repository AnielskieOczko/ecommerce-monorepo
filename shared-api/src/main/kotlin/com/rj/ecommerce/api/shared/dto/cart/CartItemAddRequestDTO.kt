package com.rj.ecommerce.api.shared.dto.cart

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
data class CartItemAddRequestDTO(
    val productId: Long,
    val quantity: Int
) {
    init {
        require(quantity >= 1) { "Quantity must be at least 1" }
    }
}
