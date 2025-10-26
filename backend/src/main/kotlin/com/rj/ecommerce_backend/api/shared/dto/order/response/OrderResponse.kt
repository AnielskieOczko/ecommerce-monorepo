package com.rj.ecommerce_backend.api.shared.dto.order.response

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.dto.product.common.ProductSummary
import com.rj.ecommerce.api.shared.enums.CanonicalPaymentStatus
import com.rj.ecommerce.api.shared.enums.OrderStatus
import com.rj.ecommerce.api.shared.enums.PaymentMethod
import com.rj.ecommerce.api.shared.enums.ShippingMethod
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "A detailed representation of a customer order.")
data class OrderResponse(
    @field:Schema(description = "The unique ID of the order.", example = "1001")
    val id: Long?,

    @field:Schema(description = "The ID of the user who placed the order.", example = "42")
    val userId: Long?,

    @field:Schema(description = "The email of the customer who placed the order.", example = "customer@example.com")
    val customerEmail: String?,

    @field:Schema(description = "A list of all items included in the order.")
    val items: List<OrderItemDetails>,

    @field:Schema(description = "The total monetary value of the order.")
    val totalAmount: Money?,

    @field:Schema(description = "The address where the order will be shipped.")
    val shippingAddress: Address?,

//    @field:Schema(description = "The billing address for the order.")
//    val billingAddress: Address?,

    @field:Schema(description = "The shipping method selected for the order.")
    val shippingMethod: ShippingMethod?,

    @field:Schema(description = "The payment method used for the order.")
    val paymentMethod: PaymentMethod?,

    @field:Schema(description = "The current status of the order processing and fulfillment.")
    val orderStatus: OrderStatus,

    @field:Schema(description = "The canonical status of the payment for this order.")
    val paymentStatus: CanonicalPaymentStatus,

    @field:Schema(description = "The unique transaction ID from the payment provider.", example = "pi_3JZ... or ch_1IZ...")
    val paymentTransactionId: String?,

    @field:Schema(description = "The URL for the payment provider's checkout page, if applicable.")
    val checkoutSessionUrl: String?,

    @field:Schema(description = "The URL to view the payment receipt, if available.")
    val receiptUrl: String?,

    @field:Schema(description = "The timestamp when the order was placed.")
    val orderDate: LocalDateTime?,

    @field:Schema(description = "The timestamp when the checkout session expires.")
    val checkoutSessionExpiresAt: LocalDateTime?
)

@Schema(description = "Represents a single line item within an order response.")
data class OrderItemDetails(
    @field:Schema(description = "A summary of the product for this line item.")
    val product: ProductSummary,

    @field:Schema(description = "The number of units ordered for this product.", example = "2")
    val quantity: Int,

    @field:Schema(description = "The total price for this line item (unit price * quantity).")
    val lineTotal: Money
)