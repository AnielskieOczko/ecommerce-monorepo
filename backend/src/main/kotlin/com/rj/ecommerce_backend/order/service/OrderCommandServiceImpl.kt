package com.rj.ecommerce_backend.order.service

import com.rj.ecommerce.api.shared.dto.order.OrderDTO
import com.rj.ecommerce.api.shared.enums.*
import com.rj.ecommerce.api.shared.messaging.payment.PaymentResponseDTO
import com.rj.ecommerce_backend.notification.dispatcher.OrderNotificationDispatcher
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.order.domain.events.CancellationActor
import com.rj.ecommerce_backend.order.domain.events.OrderCancelledEvent
import com.rj.ecommerce_backend.order.exception.AccessDeniedException
import com.rj.ecommerce_backend.order.exception.OrderCancellationException
import com.rj.ecommerce_backend.order.exception.OrderNotFoundException
import com.rj.ecommerce_backend.order.exception.OrderServiceException
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
    private val orderNotificationDispatcher: OrderNotificationDispatcher,
    private val eventPublisher: ApplicationEventPublisher
) : OrderCommandService {

    @Transactional
    override fun updateOrderStatus(orderId: Long, newStatus: OrderStatus): OrderDTO {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }

        // The business logic to decide IF and WHAT to send lives here.
        val template: NotificationTemplate? = when (newStatus) {
            OrderStatus.SHIPPED -> NotificationTemplate.ORDER_SHIPPED
            OrderStatus.DELIVERED -> NotificationTemplate.ORDER_DELIVERED
            OrderStatus.REFUNDED -> NotificationTemplate.ORDER_REFUNDED
            else -> null // Do not send notifications for other statuses like PROCESSING
        }

        // Update the order status
        order.orderStatus = newStatus
        val updatedOrder = orderRepository.save(order)

        // If a template was selected, call the dispatcher.
        template?.let {
            orderNotificationDispatcher.sendOrderStatusUpdate(updatedOrder, it)
        }

        return orderMapper.toDto(updatedOrder)
            ?: throw OrderServiceException("Failed to map updated order $orderId to DTO.")
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
    override fun updateOrderWithCheckoutSession(response: PaymentResponseDTO) {
        val order = orderRepository.findById(response.orderId).orElseThrow { OrderNotFoundException(response.orderId) }
        order.paymentStatus = response.paymentStatus
        order.paymentTransactionId = response.sessionId
        order.checkoutSessionUrl = response.checkoutUrl
        order.checkoutSessionExpiresAt = response.expiresAt
        order.receiptUrl = response.metadata?.get("receiptUrl")

        when (response.paymentStatus) {
            CanonicalPaymentStatus.SUCCEEDED -> {
                order.orderStatus = OrderStatus.CONFIRMED
                orderNotificationDispatcher.sendPaymentSuccess(order)
            }
            CanonicalPaymentStatus.FAILED, CanonicalPaymentStatus.EXPIRED -> {
                order.orderStatus = OrderStatus.FAILED
                orderNotificationDispatcher.sendPaymentFailed(order)
            }
            CanonicalPaymentStatus.CANCELED -> {
                // If the user cancels on the Stripe page, we might want to cancel our order too.
                order.orderStatus = OrderStatus.CANCELLED
                logger.info { "Order ${order.id} was cancelled by user during payment." }
                // Optionally send a cancellation notification
                orderNotificationDispatcher.sendOrderCancelled(order)
            }
            CanonicalPaymentStatus.PENDING -> {
                // The payment is still processing (e.g., bank transfer).
                // We keep our order status as PENDING. No state change needed.
                logger.info { "Payment for order ${order.id} is PENDING. No status change." }
            }
            CanonicalPaymentStatus.UNKNOWN -> {
                // An unknown status is a serious issue. Log it as an error for investigation.
                logger.error { "Received UNKNOWN payment status for order ${order.id}. Manual investigation required." }
            }
        }
        orderRepository.save(order)
    }

    @Transactional
    override fun updatePaymentDetailsOnInitiation(order: Order) {
        order.paymentStatus = CanonicalPaymentStatus.PENDING
        orderRepository.save(order)
    }


}