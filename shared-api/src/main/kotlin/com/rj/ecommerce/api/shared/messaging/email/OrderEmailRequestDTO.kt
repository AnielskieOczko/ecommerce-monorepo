package com.rj.ecommerce.api.shared.messaging.email

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.dto.customer.CustomerInfo
import com.rj.ecommerce.api.shared.dto.order.OrderItemDTO
import com.rj.ecommerce.api.shared.enums.EmailTemplate
import com.rj.ecommerce.api.shared.enums.OrderStatus
import com.rj.ecommerce.api.shared.enums.PaymentMethod
import com.rj.ecommerce.api.shared.enums.ShippingMethod
import java.time.LocalDateTime
import java.util.UUID

/**
 * Request to send an order-related email.
 *
 * @property messageId Unique ID for this message.
 * @property version Message format version.
 * @property to Email address of the recipient.
 * @property subject Optional subject line (may be generated from template).
 * @property template Email template to use.
 * @property additionalData Additional context-specific data for the template.
 * @property timestamp Time when the message was created.
 * @property orderId ID of the order this email relates to.
 * @property orderNumber Human-readable order number.
 * @property customer Customer information.
 * @property items List of items in the order.
 * @property totalAmount Total amount of the order.
 * @property shippingAddress Shipping address for the order.
 * @property billingAddress Billing address for the order.
 * @property shippingMethod Method used for shipping.
 * @property paymentMethod Method used for payment.
 * @property orderStatus Current status of the order.
 *
 * Requirements:
 * - messageId, version, to, template, timestamp, orderId, customer, items, totalAmount, and shippingAddress are required
 * - subject, additionalData, orderNumber, billingAddress, shippingMethod, paymentMethod, and orderStatus are optional
 */
data class OrderEmailRequest(
    val messageId: UUID,
    val version: String,
    val to: String,
    val subject: String? = null,
    val template: EmailTemplate,
    val additionalData: Map<String, Any>? = null,
    val timestamp: LocalDateTime,
    val orderId: String,
    val orderNumber: String? = null,
    val customer: CustomerInfo,
    val items: List<OrderItemDTO>,
    val totalAmount: Money,
    val shippingAddress: Address,
    val billingAddress: Address? = null,
    val shippingMethod: ShippingMethod? = null,
    val paymentMethod: PaymentMethod? = null,
    val orderStatus: OrderStatus? = null
)
