package com.rj.ecommerce_backend.order.listeners

import com.rj.ecommerce_backend.api.shared.enums.NotificationChannel
import com.rj.ecommerce_backend.api.shared.enums.NotificationEntityType
import com.rj.ecommerce_backend.api.shared.enums.NotificationTemplate
import com.rj.ecommerce_backend.api.shared.enums.OrderStatus
import com.rj.ecommerce_backend.events.order.OrderCancelledEvent
import com.rj.ecommerce_backend.events.order.OrderCreatedEvent
import com.rj.ecommerce_backend.events.order.OrderStatusChangedEvent
import com.rj.ecommerce_backend.events.payment.PaymentFailedEvent
import com.rj.ecommerce_backend.events.payment.PaymentSucceededEvent
import com.rj.ecommerce_backend.notification.command.CreateNotificationCommand
import com.rj.ecommerce_backend.notification.context.NotificationContext
import com.rj.ecommerce_backend.notification.service.NotificationService
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class OrderEventListener(
    private val notificationService: NotificationService
) {

    @TransactionalEventListener
    fun onOrderCreated(event: OrderCreatedEvent) {
        val order = event.order
        val user = order.user ?: return

        val command = CreateNotificationCommand(
            recipient = user.email.value,
            subject = "Your Order Confirmation #${order.id}",
            template = NotificationTemplate.ORDER_CONFIRMATION,
            entityType = NotificationEntityType.ORDER,
            entityId = order.id.toString(),
            context = NotificationContext.OrderContext(order),
            channels = setOf(NotificationChannel.EMAIL, NotificationChannel.SMS)
        )
        notificationService.dispatch(command)
    }

    @TransactionalEventListener
    fun onOrderCancelled(event: OrderCancelledEvent) {
        val order = event.order
        val user = order.user ?: return

        val command = CreateNotificationCommand(
            recipient = user.email.value,
            subject = "Your Order #${order.id} Has Been Cancelled",
            template = NotificationTemplate.ORDER_CANCELLED,
            entityType = NotificationEntityType.ORDER,
            entityId = order.id.toString(),
            context = NotificationContext.OrderContext(order),
            channels = setOf(NotificationChannel.EMAIL)
        )
        notificationService.dispatch(command)
    }

    @TransactionalEventListener
    fun onPaymentSucceeded(event: PaymentSucceededEvent) {
        val order = event.order
        val user = order.user ?: return

        val command = CreateNotificationCommand(
            recipient = user.email.value,
            subject = "Payment Received for Order #${order.id}",
            template = NotificationTemplate.PAYMENT_CONFIRMATION,
            entityType = NotificationEntityType.ORDER,
            entityId = order.id.toString(),
            context = NotificationContext.OrderContext(order),
            channels = setOf(NotificationChannel.EMAIL)
        )
        notificationService.dispatch(command)
    }

    @TransactionalEventListener
    fun onPaymentFailed(event: PaymentFailedEvent) {
        val order = event.order
        val user = order.user ?: return

        val command = CreateNotificationCommand(
            recipient = user.email.value,
            subject = "Payment Failed for Order #${order.id}",
            template = NotificationTemplate.PAYMENT_FAILED,
            entityType = NotificationEntityType.ORDER,
            entityId = order.id.toString(),
            context = NotificationContext.OrderContext(order),
            channels = setOf(NotificationChannel.EMAIL)
        )
        notificationService.dispatch(command)
    }

    @TransactionalEventListener
    fun onOrderStatusChanged(event: OrderStatusChangedEvent) {
        val order = event.order
        val user = order.user ?: return

        // Business logic: Decide which status changes warrant a notification.
        val template: NotificationTemplate = when (event.newStatus) {
            OrderStatus.SHIPPED -> NotificationTemplate.ORDER_SHIPPED
            OrderStatus.DELIVERED -> NotificationTemplate.ORDER_DELIVERED
            OrderStatus.REFUNDED -> NotificationTemplate.ORDER_REFUNDED
            // For other statuses like PROCESSING, we choose not to send a notification.
            else -> return
        }

        val command = CreateNotificationCommand(
            recipient = user.email.value,
            subject = "Update on your order #${order.id}",
            template = template,
            entityType = NotificationEntityType.ORDER,
            entityId = order.id.toString(),
            context = NotificationContext.OrderStatusUpdateContext(order, event.previousStatus),
            channels = setOf(NotificationChannel.EMAIL)
        )
        notificationService.dispatch(command)
    }
}