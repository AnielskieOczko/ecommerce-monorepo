package com.rj.ecommerce_backend.order.service

import com.rj.ecommerce.api.shared.dto.cart.CartItemDTO
import com.rj.ecommerce.api.shared.dto.order.OrderCreateRequestDTO
import com.rj.ecommerce.api.shared.dto.order.OrderDTO
import com.rj.ecommerce_backend.order.search.OrderSearchCriteria
import com.rj.ecommerce.api.shared.enums.OrderStatus
import com.rj.ecommerce.api.shared.messaging.payment.PaymentResponseDTO
import com.rj.ecommerce_backend.order.domain.Order
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.math.BigDecimal

interface OrderService {
    fun updatePaymentDetailsOnInitiation(order: Order)

    fun updateOrderWithCheckoutSession(response: PaymentResponseDTO)

    fun createOrder(userId: Long, orderCreateRequestDTO: OrderCreateRequestDTO): OrderDTO

    fun getOrderById(userId: Long, orderId: Long): Order?

    fun getOrderByIdWithOrderItems(orderId: Long): Order?

    fun getAllOrders(pageable: Pageable, criteria: OrderSearchCriteria): Page<OrderDTO>

    fun getOrdersForUser(pageable: Pageable, criteria: OrderSearchCriteria): Page<OrderDTO>

    fun updateOrderStatus(orderId: Long, newStatus: OrderStatus): OrderDTO // Parameters & return non-nullable

    fun cancelOrder(userId: Long, orderId: Long) // Parameters non-nullable

    fun calculateOrderTotal(cartItems: List<CartItemDTO>): BigDecimal

    // Admin only methods
    fun cancelOrderAdmin(orderId: Long)

    // TODO: admin should also have in path userId (This is a controller concern, service takes what it needs)
    fun getOrderByIdAdmin(orderId: Long): Order?
}
