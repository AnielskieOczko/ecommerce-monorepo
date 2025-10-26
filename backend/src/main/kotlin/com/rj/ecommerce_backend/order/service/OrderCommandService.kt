package com.rj.ecommerce_backend.order.service

import com.rj.ecommerce_backend.api.shared.dto.order.response.OrderResponse
import com.rj.ecommerce_backend.api.shared.enums.CanonicalPaymentStatus
import com.rj.ecommerce_backend.api.shared.enums.OrderStatus
import com.rj.ecommerce_backend.order.domain.Order
import java.time.LocalDateTime


    interface OrderCommandService {

        fun updateOrderStatus(orderId: Long, newStatus: OrderStatus): OrderResponse

        fun cancelOrder(userId: Long, orderId: Long)

        fun cancelOrderAdmin(orderId: Long)

        // The new method for the synchronous payment initiation
        fun updateOrderWithPaymentSession(
            order: Order,
            sessionId: String,
            sessionUrl: String,
            expiresAt: LocalDateTime
        )

        // The new method for the asynchronous webhook callback
        fun updateOrderStatusFromPayment(
            orderId: Long,
            newStatus: CanonicalPaymentStatus,
            transactionId: String?
        )
    }

