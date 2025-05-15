package com.rj.ecommerce_backend.order.service

import com.rj.ecommerce.api.shared.dto.cart.CartItemDTO
import com.rj.ecommerce_backend.messaging.payment.dto.CheckoutSessionResponseDTO
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.order.dtos.OrderCreationRequest
import com.rj.ecommerce_backend.order.dtos.OrderDTO
import com.rj.ecommerce_backend.order.enums.OrderStatus
import com.rj.ecommerce_backend.order.search.OrderSearchCriteria
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.util.*

interface OrderService {
    fun updatePaymentDetailsOnInitiation(order: Order?)

    fun updateOrderWithCheckoutSession(response: CheckoutSessionResponseDTO?)

    fun createOrder(userId: Long?, orderCreationRequest: OrderCreationRequest?): OrderDTO?

    fun getOrderById(userId: Long?, orderId: Long?): Optional<Order?>?

    fun getOrderByIdWithOrderItems(orderId: Long?): Optional<Order?>?

    fun getAllOrders(pageable: Pageable?, criteria: OrderSearchCriteria?): Page<OrderDTO?>?

    fun getOrdersForUser(pageable: Pageable?, criteria: OrderSearchCriteria?): Page<OrderDTO?>?

    fun updateOrderStatus(orderId: Long?, newStatus: OrderStatus?): OrderDTO?

    fun cancelOrder(userId: Long?, orderId: Long?)

    fun calculateOrderTotal(cartItems: MutableList<CartItemDTO?>?): BigDecimal?


    // Admin only methods
    fun cancelOrderAdmin(orderId: Long?)

    // TODO: admin should also have in path userId
    fun getOrderByIdAdmin(orderId: Long?): Optional<Order?>?
}
