package com.rj.ecommerce_backend.api.shared.dto.cart.response

import com.rj.ecommerce.api.shared.dto.product.common.ProductSummary
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Represents a user's shopping cart.")
data class CartResponse(
    @field:Schema(description = "The unique ID of the cart.", example = "5")
    val id: Long?,

    @field:Schema(description = "The ID of the user who owns the cart.", example = "42")
    val userId: Long?,

    @field:Schema(description = "A list of all items currently in the cart.")
    val items: List<CartItemDetails>,

    @field:Schema(description = "The date and time when the cart was created.")
    val createdAt: LocalDateTime?,

    @field:Schema(description = "The date and time when the cart was last updated.")
    val updatedAt: LocalDateTime?
)

@Schema(description = "Represents a single item within a shopping cart.")
data class CartItemDetails(
    @field:Schema(description = "The unique ID of this specific cart item entry.", example = "17")
    val id: Long?,

    @field:Schema(description = "A summary of the product associated with this cart item.")
    val product: ProductSummary,

    @field:Schema(description = "The number of units of the product in the cart.", example = "2")
    val quantity: Int
)