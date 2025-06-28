package com.rj.ecommerce_backend.order.service

import com.rj.ecommerce.api.shared.dto.order.OrderDTO
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.order.exception.AccessDeniedException
import com.rj.ecommerce_backend.order.mapper.OrderMapper
import com.rj.ecommerce_backend.order.repository.OrderRepository
import com.rj.ecommerce_backend.order.search.OrderSearchCriteria
import com.rj.ecommerce_backend.security.SecurityContext
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
@Transactional(readOnly = true)
class OrderQueryServiceImpl(
    private val orderRepository: OrderRepository,
    private val orderMapper: OrderMapper,
    private val securityContext: SecurityContext
) : OrderQueryService {

    override fun getOrderById(userId: Long, orderId: Long): OrderDTO? {
        securityContext.ensureAccess(userId)
        val order = orderRepository.findById(orderId).orElse(null) ?: return null
        if (order.user?.id != userId) {
            logger.warn { "User $userId attempted to access order $orderId belonging to user ${order.user?.id}" }
            throw AccessDeniedException("User $userId is not authorized to access order $orderId")
        }
        // CORRECT: Map the found entity to a DTO before returning.
        return orderMapper.toDto(order)
    }

    override fun getOrderByIdAdmin(orderId: Long): OrderDTO? {
        securityContext.ensureAdmin()
        // CORRECT: Use .map to safely transform the Optional<Order> to an Optional<OrderDTO>
        return orderRepository.findById(orderId)
            .map { orderMapper.toDto(it) }
            .orElse(null)
    }

    override fun getOrderByIdWithOrderItems(orderId: Long): OrderDTO? {
        val currentUser = securityContext.getCurrentUser()
        val userId = currentUser.id ?: throw IllegalStateException("Authenticated user has a null ID.")

        securityContext.ensureAccess(userId)

        // CORRECT: Map the found entity to a DTO before returning.
        return orderRepository.findByIdWithOrderItems(orderId, userId)
            ?.let { orderMapper.toDto(it) }
    }

    override fun getOrdersForUser(pageable: Pageable, criteria: OrderSearchCriteria): Page<OrderDTO> {
        criteria.userId?.let { securityContext.ensureAccess(it) }
            ?: throw IllegalArgumentException("User ID must be provided for this query.")

        val spec: Specification<Order> = criteria.toSpecification()
        val pageOfOrders = orderRepository.findAll(spec, pageable)

        // This was already correct: .map on a Page transforms its content.
        return pageOfOrders.map { orderMapper.toDto(it) }
    }

    override fun getAllOrders(pageable: Pageable, criteria: OrderSearchCriteria): Page<OrderDTO> {
        securityContext.ensureAdmin()
        val spec: Specification<Order> = criteria.toSpecification()
        val pageOfOrders = orderRepository.findAll(spec, pageable)

        // This was already correct.
        return pageOfOrders.map { orderMapper.toDto(it) }
    }
}