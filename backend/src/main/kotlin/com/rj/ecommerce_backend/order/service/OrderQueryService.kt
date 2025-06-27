package com.rj.ecommerce_backend.order.service

import com.rj.ecommerce.api.shared.dto.order.OrderDTO
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.order.search.OrderSearchCriteria
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface OrderQueryService {
    fun getOrderById(userId: Long, orderId: Long): Order?
    fun getOrderByIdAdmin(orderId: Long): Order?
    fun getOrderByIdWithOrderItems(orderId: Long): Order?
    fun getOrdersForUser(pageable: Pageable, criteria: OrderSearchCriteria): Page<OrderDTO>
    fun getAllOrders(pageable: Pageable, criteria: OrderSearchCriteria): Page<OrderDTO>
}