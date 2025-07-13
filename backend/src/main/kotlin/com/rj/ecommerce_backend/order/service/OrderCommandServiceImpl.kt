package com.rj.ecommerce_backend.order.service

import com.rj.ecommerce.api.shared.dto.order.response.OrderResponse
import com.rj.ecommerce.api.shared.enums.*
import com.rj.ecommerce.api.shared.messaging.payment.response.PaymentInitiationResponse
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.events.CancellationActor
import com.rj.ecommerce_backend.events.order.OrderCancelledEvent
import com.rj.ecommerce_backend.events.order.OrderStatusChangedEvent
import com.rj.ecommerce_backend.events.payment.PaymentFailedEvent
import com.rj.ecommerce_backend.events.payment.PaymentSucceededEvent
import com.rj.ecommerce_backend.order.exception.OrderNotFoundException
import com.rj.ecommerce_backend.order.mapper.OrderMapper
import com.rj.ecommerce_backend.order.repository.OrderRepository
import com.rj.ecommerce_backend.security.SecurityContext
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class OrderCommandServiceImpl(
    private val orderRepository: OrderRepository,
    private val securityContext: SecurityContext,
    private val orderMapper: OrderMapper,
    private val eventPublisher: ApplicationEventPublisher
) : OrderCommandService {

    @Transactional
    override fun updateOrderStatus(orderId: Long, newStatus: OrderStatus): OrderResponse {
        val order = orderRepository.findById(orderId).orElseThrow { OrderNotFoundException(orderId) }
        val previousStatus = order.orderStatus

        order.orderStatus = newStatus
        val updatedOrder = orderRepository.save(order)

        // PUBLISH EVENT
        eventPublisher.publishEvent(OrderStatusChangedEvent(this, updatedOrder, newStatus, previousStatus))

        return orderMapper.toDto(updatedOrder)
    }

    @Transactional
    override fun cancelOrder(userId: Long, orderId: Long) {
        val order = orderRepository.findById(orderId).orElseThrow { OrderNotFoundException(orderId) }
        // ... (validation logic) ...

        order.orderStatus = OrderStatus.CANCELLED
        val savedOrder = orderRepository.save(order)

        // PUBLISH EVENT instead of calling the dispatcher directly
        eventPublisher.publishEvent(OrderCancelledEvent(this, savedOrder, CancellationActor.USER))
    }

    @Transactional
    override fun cancelOrderAdmin(orderId: Long) {
        securityContext.ensureAdmin()
        val order = orderRepository.findById(orderId).orElseThrow { OrderNotFoundException(orderId) }
        // ... (validation logic) ...

        order.orderStatus = OrderStatus.CANCELLED
        val savedOrder = orderRepository.save(order)

        // PUBLISH EVENT instead of calling the dispatcher directly
        eventPublisher.publishEvent(OrderCancelledEvent(this, savedOrder, CancellationActor.ADMIN))
    }

    @Transactional
    override fun updateOrderWithCheckoutSession(response: PaymentInitiationResponse) {
        val order = orderRepository.findById(response.orderId).orElseThrow { OrderNotFoundException(response.orderId) }
        // ... (update order fields)

        when (response.paymentStatus) {
            CanonicalPaymentStatus.SUCCEEDED -> {
                order.orderStatus = OrderStatus.CONFIRMED
                // PUBLISH EVENT
                eventPublisher.publishEvent(PaymentSucceededEvent(this, order, response))
            }
            CanonicalPaymentStatus.FAILED, CanonicalPaymentStatus.EXPIRED -> {
                order.orderStatus = OrderStatus.FAILED
                // PUBLISH EVENT
                eventPublisher.publishEvent(PaymentFailedEvent(this, order, response))
            }
            CanonicalPaymentStatus.CANCELED -> {
                order.orderStatus = OrderStatus.CANCELLED
                // PUBLISH EVENT
                eventPublisher.publishEvent(OrderCancelledEvent(this, order, CancellationActor.SYSTEM))
            }
            else -> { /* No event for PENDING or UNKNOWN */ }
        }
        orderRepository.save(order)
    }

    @Transactional
    override fun updatePaymentDetailsOnInitiation(order: Order) {
        order.paymentStatus = CanonicalPaymentStatus.PENDING
        orderRepository.save(order)
    }


}