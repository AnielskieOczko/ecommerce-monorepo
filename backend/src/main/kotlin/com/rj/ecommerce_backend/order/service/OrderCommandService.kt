package com.rj.ecommerce_backend.order.service

import com.rj.ecommerce.api.shared.dto.order.OrderDTO
import com.rj.ecommerce.api.shared.enums.OrderStatus
import com.rj.ecommerce.api.shared.messaging.payment.PaymentResponseDTO
import com.rj.ecommerce_backend.order.domain.Order

interface OrderCommandService {
    fun updatePaymentDetailsOnInitiation(order: Order)
    fun updateOrderWithCheckoutSession(response: PaymentResponseDTO)
    fun updateOrderStatus(orderId: Long, newStatus: OrderStatus): OrderDTO
    fun cancelOrder(userId: Long, orderId: Long)
    fun cancelOrderAdmin(orderId: Long)
}
