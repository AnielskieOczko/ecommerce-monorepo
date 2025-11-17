package com.rj.ecommerce_backend.notification.dispatcher

import com.rj.ecommerce_backend.api.shared.enums.NotificationChannel
import com.rj.ecommerce_backend.api.shared.enums.NotificationEntityType
import com.rj.ecommerce_backend.api.shared.enums.NotificationTemplate
import com.rj.ecommerce_backend.notification.command.CreateNotificationCommand
import com.rj.ecommerce_backend.notification.context.NotificationContext
import com.rj.ecommerce_backend.notification.service.NotificationService
import com.rj.ecommerce_backend.order.domain.Order
import org.springframework.stereotype.Component

@Component
class PaymentNotificationDispatcher(private val notificationService: NotificationService) {

    fun sendPaymentSuccess(order: Order) {
        val user = order.user ?: return
        val command = CreateNotificationCommand(
            recipient = user.email.value,
            subject = "Payment Received for Order #${order.id}",
            template = NotificationTemplate.PAYMENT_CONFIRMATION,
            entityType = NotificationEntityType.PAYMENT,
            entityId = order.paymentTransactionId ?: order.id.toString(),
            context = NotificationContext.OrderContext(order),
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
            entityType = NotificationEntityType.PAYMENT,
            entityId = order.paymentTransactionId ?: order.id.toString(),
            context = NotificationContext.OrderContext(order),
            channels = setOf(NotificationChannel.EMAIL)
        )
        notificationService.dispatch(command)
    }
}