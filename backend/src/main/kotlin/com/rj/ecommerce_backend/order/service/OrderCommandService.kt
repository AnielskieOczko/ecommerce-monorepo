package com.rj.ecommerce_backend.order.service

import com.rj.ecommerce.api.shared.dto.order.response.OrderResponse
import com.rj.ecommerce.api.shared.enums.OrderStatus
import com.rj.ecommerce.api.shared.messaging.payment.response.PaymentInitiationResponse
import com.rj.ecommerce_backend.order.domain.Order

interface OrderCommandService {
    fun updatePaymentDetailsOnInitiation(order: Order)
    fun updateOrderWithCheckoutSession(response: PaymentInitiationResponse)
    fun updateOrderStatus(orderId: Long, newStatus: OrderStatus): OrderResponse
    fun cancelOrder(userId: Long, orderId: Long)
    fun cancelOrderAdmin(orderId: Long)
}
