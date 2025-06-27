package com.rj.ecommerce.api.shared.dto.order

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.dto.customer.CustomerInfoDTO
import com.rj.ecommerce.api.shared.enums.OrderStatus
import com.rj.ecommerce.api.shared.enums.PaymentMethod
import com.rj.ecommerce.api.shared.enums.ShippingMethod
import java.time.LocalDateTime

/**
 * A type-safe, internal DTO to carry structured data for an order notification.
 * This serves as the contract between the OrderNotificationMapper and any provider that consumes it.
 */
data class OrderNotificationPayload(
    val to: String,
    val orderId: Long,
    val orderNumber: String?,
    val customer: CustomerInfoDTO,
    val items: List<MessagingOrderItemDTO>,
    val totalAmount: Money,
    val shippingAddress: Address,
    val shippingMethod: ShippingMethod,
    val paymentMethod: PaymentMethod,
    val paymentTransactionId: String?,
    val orderDate: LocalDateTime,
    val orderStatus: OrderStatus
)