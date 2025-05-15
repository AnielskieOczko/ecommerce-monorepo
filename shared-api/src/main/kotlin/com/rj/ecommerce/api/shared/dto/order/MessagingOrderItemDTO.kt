package com.rj.ecommerce.api.shared.dto.order // Or a sub-package like common

import com.rj.ecommerce.api.shared.core.Money
import jakarta.validation.constraints.Min

// Renamed to avoid conflict with a potentially different shared OrderItemDTO
data class MessagingOrderItemDTO(
    val id: String? = null, // Nullable, as per your original logic
    // @field:NotBlank // if productId should not be blank
    val productId: String,
    val productName: String,
    val productSku: String? = null, // Nullable
    @field:Min(1) // Quantity must be positive
    val quantity: Int,
    val unitPrice: Money, // Assuming MoneyDTO is a non-nullable data class
    val totalPrice: Money? = null // Often calculated, can be nullable
) {
    init {
        require(productId.isNotBlank()) { "Product ID cannot be blank" }
        require(productName.isNotBlank()) { "Product name cannot be blank" }
        require(quantity > 0) { "Quantity must be positive" }
        // id and productSku are handled by nullable types and defaults.
    }
}