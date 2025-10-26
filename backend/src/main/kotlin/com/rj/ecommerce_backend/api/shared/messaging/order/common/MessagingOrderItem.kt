package com.rj.ecommerce_backend.api.shared.messaging.order.common

import com.rj.ecommerce.api.shared.core.Money
import jakarta.validation.constraints.Min

/**
 * A representation of an order item specifically for messaging purposes.
 */
data class MessagingOrderItem(
    val id: String?,
    val productId: String,
    val productName: String,
    val productSku: String? = null,
    @field:Min(1)
    val quantity: Int,
    val unitPrice: Money,
    val totalPrice: Money? = null
) {
    init {
        require(productId.isNotBlank()) { "Product ID cannot be blank" }
        require(productName.isNotBlank()) { "Product name cannot be blank" }
        require(quantity > 0) { "Quantity must be positive" }
    }
}