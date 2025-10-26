package com.rj.ecommerce_backend.api.shared.messaging.notification.payload

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.dto.customer.common.CustomerInfo
import com.rj.ecommerce.api.shared.enums.OrderStatus
import com.rj.ecommerce.api.shared.enums.PaymentMethod
import com.rj.ecommerce.api.shared.enums.ShippingMethod
import com.rj.ecommerce.api.shared.messaging.order.common.MessagingOrderItem
import java.time.LocalDateTime

/**
 * Holds the specific, channel-agnostic data required for an order-related notification.
 * This is the "what" of the message.
 */
data class OrderPayload(
    val orderId: String,
    val orderNumber: String?,
    val customer: CustomerInfo?,
    val items: List<MessagingOrderItem>,
    val totalAmount: Money,
    val shippingAddress: Address?,
    val shippingMethod: ShippingMethod?,
    val paymentMethod: PaymentMethod?,
    val paymentTransactionId: String?,
    val orderDate: LocalDateTime,
    val orderStatus: OrderStatus,
    val additionalData: Map<String, Any> = emptyMap()
)