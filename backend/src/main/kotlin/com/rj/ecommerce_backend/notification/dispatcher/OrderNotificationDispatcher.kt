package com.rj.ecommerce_backend.notification.dispatcher

import com.rj.ecommerce.api.shared.enums.*
import com.rj.ecommerce_backend.notification.Notification
import com.rj.ecommerce_backend.notification.NotificationContext
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
        val context = NotificationContext.OrderContext(order)
        val notification = Notification(
            recipient = user.email.value,
            subject = "Your Order Confirmation #${order.id}",
            channel = NotificationChannel.EMAIL,
            template = NotificationTemplate.ORDER_CONFIRMATION,
            entityType = NotificationEntityType.ORDER,
            entityId = order.id.toString(),
            context = context
        )
        dispatch(notification, "order confirmation")
    }

    fun sendPaymentSuccess(order: Order) {
        val user = order.user ?: return
        val context = NotificationContext.OrderContext(order)
        val notification = Notification(
            recipient = user.email.value,
            subject = "Payment Received for Order #${order.id}",
            channel = NotificationChannel.EMAIL,
            template = NotificationTemplate.PAYMENT_CONFIRMATION,
            entityType = NotificationEntityType.ORDER,
            entityId = order.id.toString(),
            context = context
        )
        dispatch(notification, "payment success")
    }

    fun sendPaymentFailed(order: Order) {
        val user = order.user ?: return
        val context = NotificationContext.OrderContext(order)
        val notification = Notification(
            recipient = user.email.value,
            subject = "Payment Failed for Order #${order.id}",
            channel = NotificationChannel.EMAIL,
            template = NotificationTemplate.PAYMENT_FAILED,
            entityType = NotificationEntityType.ORDER,
            entityId = order.id.toString(),
            context = context
        )
        dispatch(notification, "payment failure")
    }

    fun sendOrderCancelled(order: Order) {
        val user = order.user ?: return
        val context = NotificationContext.OrderContext(order)
        val notification = Notification(
            recipient = user.email.value,
            subject = "Your Order #${order.id} Has Been Cancelled",
            channel = NotificationChannel.EMAIL,
            template = NotificationTemplate.ORDER_CANCELLED,
            entityType = NotificationEntityType.ORDER,
            entityId = order.id.toString(),
            context = context
        )
        dispatch(notification, "order cancellation")
    }

    /**
     * A generic method to send a status update. The caller decides which template to use.
     */
    fun sendOrderStatusUpdate(order: Order, template: NotificationTemplate) {
        val user = order.user ?: return
        // For status updates, the simple OrderContext is sufficient.
        // A more specific context is only needed if the template requires extra data
        // (like previousStatus), which is not the case for shipment/delivery emails.
        val context = NotificationContext.OrderContext(order)

        val notification = Notification(
            recipient = user.email.value,
            subject = "Update on your order #${order.id}",
            channel = NotificationChannel.EMAIL,
            template = template,
            entityType = NotificationEntityType.ORDER,
            entityId = order.id.toString(),
            context = context
        )
        dispatch(notification, "order status update (${template.name})")
    }



    private fun dispatch(notification: Notification, type: String) {
        try {
            notificationService.dispatch(notification)
        } catch (e: Exception) {
            logger.error(e) { "Failed to dispatch $type notification for order ID: ${notification.entityId}. The primary operation was not affected." }
        }
    }
}