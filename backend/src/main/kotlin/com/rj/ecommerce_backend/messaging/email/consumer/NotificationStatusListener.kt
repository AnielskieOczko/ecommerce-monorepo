package com.rj.ecommerce_backend.messaging.email.consumer

import com.rj.ecommerce.api.shared.messaging.email.NotificationDeliveryReceipt
import com.rj.ecommerce_backend.notification.service.NotificationService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

/**
 * A generic RabbitMQ listener that consumes delivery status updates for any notification channel.
 * Its sole responsibility is to delegate processing to the NotificationService.
 */
@Component
class NotificationStatusListener(
    private val notificationService: NotificationService // Depend on the new, generic interface
) {

    companion object {
        private val log = KotlinLogging.logger {}
    }

    /**
     * Handles an incoming delivery receipt from the message queue.
     * This method is designed to be robust. If the service layer throws an exception
     * (e.g., NotificationNotFoundException), it will propagate, triggering
     * RabbitMQ's retry/dead-lettering mechanism, preventing message loss.
     */
    @RabbitListener(queues = ["\${app.rabbitmq.notification-receipt.queue}"]) // Corrected property name
    fun handleDeliveryReceipt(receipt: NotificationDeliveryReceipt) {
        log.info { "Received delivery receipt for correlationId: ${receipt.correlationId}, channel: ${receipt.channel}, status: ${receipt.status}" }
        try {
            notificationService.updateStatusFromReceipt(receipt)
        } catch (e: Exception) {
            // SIMPLIFIED: Log a clear error message. The framework will log the stack trace.
            log.error { "Unhandled exception while processing receipt for correlationId: ${receipt.correlationId}. Message will be rejected/re-queued." }
            throw e
        }
    }

}