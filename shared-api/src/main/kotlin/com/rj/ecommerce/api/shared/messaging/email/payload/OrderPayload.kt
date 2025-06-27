package com.rj.ecommerce.api.shared.messaging.email.payload

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.dto.customer.CustomerInfoDTO
import com.rj.ecommerce.api.shared.dto.order.MessagingOrderItemDTO
import com.rj.ecommerce.api.shared.enums.OrderStatus
import com.rj.ecommerce.api.shared.enums.PaymentMethod
import com.rj.ecommerce.api.shared.enums.ShippingMethod
import java.time.LocalDateTime

/**
 * Holds the specific, channel-agnostic data required for an order-related notification.
 */
data class OrderPayload(
    val orderId: String,
    val orderNumber: String?,
    val customer: CustomerInfoDTO?,
    val items: List<MessagingOrderItemDTO>,
    val totalAmount: Money,
    val shippingAddress: Address?,
    val shippingMethod: ShippingMethod?,
    val paymentMethod: PaymentMethod?,
    val paymentTransactionId: String?,
    val orderDate: LocalDateTime,
    val orderStatus: OrderStatus,
    val additionalData: Map<String, Any> = emptyMap()
)