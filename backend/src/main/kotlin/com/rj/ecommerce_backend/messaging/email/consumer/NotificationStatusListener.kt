package com.rj.ecommerce_backend.messaging.email.consumer

import com.rj.ecommerce.api.shared.messaging.email.EmailDeliveryReceiptDTO
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
    @RabbitListener(queues = ["\${app.rabbitmq.email-notification.queue}"])
    fun handleDeliveryReceipt(receipt: EmailDeliveryReceiptDTO) {
        log.info { "Received delivery receipt for original message (correlationId): ${receipt.correlationId}, status: ${receipt.status}" }

        try {
            // The listener's responsibility is now just one line: delegate to the service.
            notificationService.updateStatusFromReceipt(receipt)
        } catch (e: Exception) {
            // This catch block is a safety net. The service should handle known exceptions.
            // Re-throwing is crucial for the messaging infrastructure to work correctly.
            log.error(e) { "Error processing receipt for correlationId: ${receipt.correlationId}. The message will be retried or sent to DLQ." }
            throw e
        }
    }
}