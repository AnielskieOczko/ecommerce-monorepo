package com.rj.ecommerce.api.shared.dto.order

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.dto.order.OrderItemDTO
import com.rj.ecommerce.api.shared.enums.CanonicalPaymentStatus
import com.rj.ecommerce.api.shared.enums.OrderStatus
import com.rj.ecommerce.api.shared.enums.PaymentMethod
import com.rj.ecommerce.api.shared.enums.PaymentStatus
import com.rj.ecommerce.api.shared.enums.ShippingMethod
import java.time.LocalDateTime

/**
 * Represents a completed order.
 *
 * @property id Order ID.
 * @property userId User ID who placed the order.
 * @property customerEmail Email of the customer who placed the order.
 * @property items List of items in the order.
 * @property totalAmount Total amount of the order.
 * @property shippingAddress Shipping address for the order.
 * @property billingAddress Billing address for the order (if different from shipping).
 * @property shippingMethod Method used for shipping.
 * @property paymentMethod Method used for payment.
 * @property orderStatus Current status of the order.
 * @property paymentStatus Current status of the payment.
 * @property paymentTransactionId ID of the payment transaction.
 * @property checkoutSessionUrl URL for the checkout session.
 * @property receiptUrl URL for the receipt.
 * @property orderDate Date and time when the order was placed.
 *
 * Requirements:
 * - id, userId, customerEmail, items, totalAmount, shippingAddress, shippingMethod, paymentMethod, orderStatus, and orderDate are required
 * - billingAddress, paymentStatus, paymentTransactionId, checkoutSessionUrl, and receiptUrl are optional
 */
data class OrderDTO(
    val id: Long? = null,
    val userId: Long?= null,
    val customerEmail: String? = null,
    val items: List<OrderItemDTO?> = mutableListOf(),
    val totalAmount: Money? = null,
    val shippingAddress: Address? = null,
    val billingAddress: Address? = null,
    val shippingMethod: ShippingMethod? = null,
    val paymentMethod: PaymentMethod? = null,
    val orderStatus: OrderStatus? = null,
    val paymentStatus: CanonicalPaymentStatus,
    val paymentTransactionId: String? = null,
    val checkoutSessionUrl: String? = null,
    val receiptUrl: String? = null,
    val orderDate: LocalDateTime? = null,
    val checkoutSessionExpiresAt: LocalDateTime? = null
)
