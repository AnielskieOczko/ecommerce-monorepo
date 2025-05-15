package com.rj.ecommerce.api.shared.dto.cart

import java.time.LocalDateTime

/**
 * Represents a user's shopping cart.
 *
 * @property id Cart ID.
 * @property userId ID of the user who owns the cart.
 * @property items List of items in the cart.
 * @property createdAt Date and time when the cart was created.
 * @property updatedAt Date and time when the cart was last updated.
 *
 * Requirements:
 * - id, userId, and items are required
 * - createdAt and updatedAt are typically auto-generated
 */
data class Cart(
    val id: Long,
    val userId: Long,
    val items: List<CartItem>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
