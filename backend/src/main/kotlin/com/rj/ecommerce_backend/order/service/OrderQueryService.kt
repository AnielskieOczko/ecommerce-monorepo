package com.rj.ecommerce_backend.order.service

import com.rj.ecommerce_backend.api.shared.dto.order.response.OrderResponse
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.order.search.OrderSearchCriteria
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface OrderQueryService {

    // --- Public API Methods (for Controllers) ---
    fun getOrderById(userId: Long, orderId: Long): OrderResponse?
    fun getOrderByIdAdmin(orderId: Long): OrderResponse?
    fun getOrderByIdWithOrderItems(orderId: Long): OrderResponse?
    fun getOrdersForUser(pageable: Pageable, criteria: OrderSearchCriteria): Page<OrderResponse>
    fun getAllOrders(pageable: Pageable, criteria: OrderSearchCriteria): Page<OrderResponse>

    // --- Internal Service Methods ---
    /**
     * [SYSTEM-LEVEL] Retrieves an Order entity by ID. No user checks performed.
     * For trusted internal processes like webhook handlers.
     */
    fun findOrderEntityById(orderId: Long): Order?

    /**
     * [USER-SCOPED] Retrieves an Order entity for a specific user.
     * For internal services that operate on behalf of a logged-in user, like PaymentFacade.
     * Performs security check to ensure user owns the order.
     */
    fun getOrderEntityByIdForUser(userId: Long, orderId: Long): Order?
}