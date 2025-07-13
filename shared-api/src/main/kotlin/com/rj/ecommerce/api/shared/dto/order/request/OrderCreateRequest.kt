package com.rj.ecommerce.api.shared.dto.order.request

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.enums.PaymentMethod
import com.rj.ecommerce.api.shared.enums.ShippingMethod
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty

@Schema(description = "Request to create a new order from a list of products.")
data class OrderCreateRequest(
    @field:Schema(description = "The shipping address for the order.", required = true)
    @field:Valid
    val shippingAddress: Address,

    @field:Schema(description = "The billing address, if different from the shipping address.")
    @field:Valid
    val billingAddress: Address? = null,

    @field:Schema(description = "The chosen payment method.", required = true)
    val paymentMethod: PaymentMethod,

    @field:Schema(description = "The chosen shipping method.", required = true)
    val shippingMethod: ShippingMethod,

    @field:Schema(description = "The list of items to be included in the order.", required = true)
    @field:NotEmpty(message = "Order must contain at least one item.")
    val items: List<OrderItemCreateRequest>
)

@Schema(description = "A single item to be included in an order creation request.")
data class OrderItemCreateRequest(
    @field:Schema(description = "The unique ID of the product.", required = true, example = "101")
    val productId: Long,

    @field:Schema(description = "The number of units for this product.", required = true, example = "2")
    @field:Min(1, message = "Quantity must be at least 1.")
    val quantity: Int
)
