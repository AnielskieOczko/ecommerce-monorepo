package com.rj.ecommerce.api.shared.dto.order

import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.dto.product.ProductSummaryDTO

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
data class OrderItemDTO(
    val product: ProductSummaryDTO,
    val quantity: Int,
    val lineTotal: Money
) {
    init {
        require(quantity >= 1) { "Quantity must be at least 1" }
    }
}