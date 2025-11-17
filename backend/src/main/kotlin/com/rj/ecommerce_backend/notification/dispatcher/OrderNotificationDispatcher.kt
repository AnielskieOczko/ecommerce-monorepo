package com.rj.ecommerce_backend.notification.dispatcher

import com.rj.ecommerce_backend.api.shared.enums.NotificationChannel
import com.rj.ecommerce_backend.api.shared.enums.NotificationEntityType
import com.rj.ecommerce_backend.api.shared.enums.NotificationTemplate
import com.rj.ecommerce_backend.notification.command.CreateNotificationCommand
import com.rj.ecommerce_backend.notification.context.NotificationContext
import com.rj.ecommerce_backend.notification.service.NotificationService
import com.rj.ecommerce_backend.order.domain.Order
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class OrderNotificationDispatcher(
    private val notificationService: NotificationService
) {
    fun sendOrderConfirmation(order: Order) {
        val user = order.user ?: return
        val command = CreateNotificationCommand(
            recipient = user.email.value,
            subject = "Your Order Confirmation #${order.id}",
            template = NotificationTemplate.ORDER_CONFIRMATION,
            entityType = NotificationEntityType.ORDER,
            entityId = order.id.toString(),
            context = NotificationContext.OrderContext(order),
            // Business decision: Order confirmations go to Email and SMS
            channels = setOf(NotificationChannel.EMAIL, NotificationChannel.SMS)
        )
        notificationService.dispatch(command)
    }

    fun sendOrderCancelled(order: Order) {
        val user = order.user ?: return
        val command = CreateNotificationCommand(
            recipient = user.email.value,
            subject = "Your Order #${order.id} Has Been Cancelled",
            template = NotificationTemplate.ORDER_CANCELLED,
            entityType = NotificationEntityType.ORDER,
            entityId = order.id.toString(),
            context = NotificationContext.OrderContext(order),
            // Business decision: Cancellations only go to Email
            channels = setOf(NotificationChannel.EMAIL)
        )
        notificationService.dispatch(command)
    }

    fun sendPaymentFailed(order: Order) {
        val user = order.user ?: return
        val command = CreateNotificationCommand(
            recipient = user.email.value,
            subject = "Payment Failed for Order #${order.id}",
            template = NotificationTemplate.PAYMENT_FAILED,
            entityType = NotificationEntityType.ORDER,
            entityId = order.id.toString(),
            context = NotificationContext.OrderContext(order),
            // Business decision: Cancellations only go to Email
            channels = setOf(NotificationChannel.EMAIL)
        )
        notificationService.dispatch(command)
    }

    /**
     * A generic method to send a status update. The caller decides which template to use.
     */
    fun sendOrderStatusUpdate(order: Order, template: NotificationTemplate) {
        val user = order.user ?: return
        // For status updates, the simple OrderContext is sufficient.
        // A more specific context is only needed if the template requires extra data
        // (like previousStatus), which is not the case for shipment/delivery emails.
        val command = CreateNotificationCommand(
            recipient = user.email.value,
            subject = "Update on your order #${order.id}",
            template = template,
            entityType = NotificationEntityType.ORDER,
            entityId = order.id.toString(),
            context = NotificationContext.OrderContext(order),
            // Business decision: Cancellations only go to Email
            channels = setOf(NotificationChannel.EMAIL)
        )
        notificationService.dispatch(command)
    }

}