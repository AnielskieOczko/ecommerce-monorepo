package com.rj.ecommerce.api.shared.dto.order

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.dto.product.OrderItem
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
data class Order(
    val id: Long,
    val userId: Long,
    val customerEmail: String,
    val items: List<OrderItem>,
    val totalAmount: Money,
    val shippingAddress: Address,
    val billingAddress: Address? = null,
    val shippingMethod: ShippingMethod,
    val paymentMethod: PaymentMethod,
    val orderStatus: OrderStatus,
    val paymentStatus: PaymentStatus? = null,
    val paymentTransactionId: String? = null,
    val checkoutSessionUrl: String? = null,
    val receiptUrl: String? = null,
    val orderDate: LocalDateTime
)
