package com.rj.ecommerce_backend.messaging.email.consumer

import com.rj.ecommerce.api.shared.enums.EmailDeliveryReceiptStatus
import com.rj.ecommerce.api.shared.messaging.email.EmailDeliveryReceiptDTO
import com.rj.ecommerce_backend.notification.service.EmailNotificationService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

/**
 * A RabbitMQ listener that consumes email delivery status updates.
 * It is responsible for updating the state of the corresponding EmailNotification
 * record in the database.
 */
@Component
class EmailStatusListener(
    // Depend on the interface, not the implementation
    private val emailNotificationService: EmailNotificationService
) {

    companion object {
        private val log = KotlinLogging.logger {}
    }

    /**
     * Handles an incoming email status DTO from the message queue.
     *
     * This method is transactional by proxy (via the service layer) and is designed to
     * be robust. If processing fails (e.g., the notification record is not found),
     * an exception is thrown, which will trigger RabbitMQ's retry/dead-lettering mechanism.
     *
     * @param statusDto The delivery status payload.
     */
    @RabbitListener(queues = ["\${app.rabbitmq.email-notification.queue}"])
    fun handleEmailStatus(receipt: EmailDeliveryReceiptDTO) {
        log.info { "Received email delivery receipt: $receipt" }

        try {
            // The listener's core responsibility: TRANSLATION
            when (receipt.status) {

                // A "DELIVERED" receipt translates to the "markAsSent" command.
                EmailDeliveryReceiptStatus.DELIVERED -> {
                    emailNotificationService.markAsSent(receipt.originalMessageId)
                }

                // A "BOUNCED" receipt translates to the "markAsFailed" command.
                EmailDeliveryReceiptStatus.BOUNCED -> {
                    val reason = receipt.errorMessage ?: "Delivery failed (bounced)."
                    emailNotificationService.markAsFailed(receipt.originalMessageId, reason)
                }

                // For other statuses like OPENED, CLICKED, we can just log them for now.
                EmailDeliveryReceiptStatus.OPENED -> log.info { "Email ${receipt.originalMessageId} was opened." }
                EmailDeliveryReceiptStatus.CLICKED -> log.info { "A link was clicked in email ${receipt.originalMessageId}." }

                else -> log.warn { "Unhandled receipt status: ${receipt.status}" }
            }

        } catch (e: Exception) {
            log.error(e) { "Error processing receipt for messageId: ${receipt.originalMessageId}" }
            throw e
        }
    }
}