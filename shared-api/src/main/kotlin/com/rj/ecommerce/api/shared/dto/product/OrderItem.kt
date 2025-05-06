package com.rj.ecommerce.api.shared.dto.product

import com.rj.ecommerce.api.shared.core.Money

/**
 * Represents an item within an order or cart.
 *
 * @property product Product information for this order item.
 * @property quantity Number of units ordered.
 * @property lineTotal Total price for this line (unitPrice * quantity).
 *
 * Requirements:
 * - product, quantity, and lineTotal are required
 * - quantity must be at least 1
 */
data class OrderItem(
    val product: ProductSummary,
    val quantity: Int,
    val lineTotal: Money
) {
    init {
        require(quantity >= 1) { "Quantity must be at least 1" }
    }
}
