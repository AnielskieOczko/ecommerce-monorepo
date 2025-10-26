package com.rj.ecommerce_backend.api.shared.messaging.notification.payload

import com.rj.ecommerce.api.shared.enums.OrderStatus

data class OrderStatusUpdatePayload(
    val orderId: String,
    val orderNumber: String?,
    val customerName: String?,
    val newStatus: OrderStatus,
    val previousStatus: OrderStatus?,
    // Optional fields for specific templates
    val trackingNumber: String? = null,
    val trackingUrl: String? = null,
    val refundAmount: String? = null
)