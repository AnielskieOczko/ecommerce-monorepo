package com.rj.ecommerce_backend.order.service

import com.rj.ecommerce_backend.api.shared.dto.order.response.OrderResponse
import com.rj.ecommerce_backend.api.shared.enums.CanonicalPaymentStatus
import com.rj.ecommerce_backend.api.shared.enums.OrderStatus
import com.rj.ecommerce_backend.events.CancellationActor
import com.rj.ecommerce_backend.events.order.OrderCancelledEvent
import com.rj.ecommerce_backend.events.order.OrderStatusChangedEvent
import com.rj.ecommerce_backend.events.payment.PaymentFailedEvent
import com.rj.ecommerce_backend.events.payment.PaymentSucceededEvent
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.order.exception.OrderNotFoundException
import com.rj.ecommerce_backend.order.mapper.OrderMapper
import com.rj.ecommerce_backend.order.repository.OrderRepository
import com.rj.ecommerce_backend.security.SecurityContext
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

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

        // Publish an event to notify other parts of the system (e.g., notifications)
        eventPublisher.publishEvent(OrderStatusChangedEvent(this, updatedOrder, newStatus, previousStatus))

        return orderMapper.toDto(updatedOrder)
    }

    @Transactional
    override fun cancelOrder(userId: Long, orderId: Long) {
        val order = orderRepository.findById(orderId).orElseThrow { OrderNotFoundException(orderId) }
        // TODO: Add validation logic here (e.g., ensure order belongs to the user, check if cancellable)

        order.orderStatus = OrderStatus.CANCELLED
        val savedOrder = orderRepository.save(order)

        // Publish an event for the cancellation
        eventPublisher.publishEvent(OrderCancelledEvent(this, savedOrder, CancellationActor.USER))
    }

    @Transactional
    override fun cancelOrderAdmin(orderId: Long) {
        securityContext.ensureAdmin()
        val order = orderRepository.findById(orderId).orElseThrow { OrderNotFoundException(orderId) }
        // TODO: Add validation logic (e.g., check if the order is in a state that can be cancelled)

        order.orderStatus = OrderStatus.CANCELLED
        val savedOrder = orderRepository.save(order)

        // Publish an event for the cancellation
        eventPublisher.publishEvent(OrderCancelledEvent(this, savedOrder, CancellationActor.ADMIN))
    }

    /**
     * NEW METHOD: Called synchronously by PaymentFacade after creating a payment session.
     */
    @Transactional
    override fun updateOrderWithPaymentSession(
        order: Order,
        sessionId: String,
        sessionUrl: String,
        expiresAt: LocalDateTime
    ) {
        logger.info { "Updating Order ID ${order.id} with new payment session. Session ID: $sessionId" }
        order.paymentStatus = CanonicalPaymentStatus.PENDING
        order.paymentTransactionId = sessionId
        order.checkoutSessionUrl = sessionUrl
        order.checkoutSessionExpiresAt = expiresAt
        orderRepository.save(order)
    }

    /**
     * NEW METHOD: Called by the payment provider's webhook handler.
     */
    @Transactional
    override fun updateOrderStatusFromPayment(
        orderId: Long,
        newStatus: CanonicalPaymentStatus,
        transactionId: String?
    ) {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }

        logger.info { "Updating Order ID $orderId from payment webhook. New payment status: $newStatus" }

        // Update the order's payment status and transaction ID
        order.paymentStatus = newStatus
        if (transactionId != null) {
            order.paymentTransactionId = transactionId
        }

        // Map the payment status to an order status and publish the relevant business event
        when (newStatus) {
            CanonicalPaymentStatus.SUCCEEDED -> {
                order.orderStatus = OrderStatus.CONFIRMED
                eventPublisher.publishEvent(PaymentSucceededEvent(
                    this, order
                ))
            }
            CanonicalPaymentStatus.FAILED, CanonicalPaymentStatus.EXPIRED -> {
                order.orderStatus = OrderStatus.FAILED
                eventPublisher.publishEvent(PaymentFailedEvent(this, order))
            }
            CanonicalPaymentStatus.CANCELED -> {
                order.orderStatus = OrderStatus.CANCELLED
                eventPublisher.publishEvent(OrderCancelledEvent(this, order, CancellationActor.SYSTEM))
            }
            else -> {
                logger.warn { "Received unhandled payment status '$newStatus' for Order ID $orderId. No order status change." }
            }
        }
        orderRepository.save(order)
    }
}