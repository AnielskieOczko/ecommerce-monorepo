package com.rj.ecommerce.api.shared.dto.cart

import com.rj.ecommerce.api.shared.dto.product.ProductSummary

/**
 * An item within a shopping cart.
 *
 * @property id Cart item ID.
 * @property product Product information for this cart item.
 * @property quantity Number of units in the cart.
 *
 * Requirements:
 * - id, product, and quantity are required
 * - quantity must be at least 1
 */
data class CartItem(
    val id: Long,
    val product: ProductSummary,
    val quantity: Int
) {
    init {
        require(quantity >= 1) { "Quantity must be at least 1" }
    }
}
