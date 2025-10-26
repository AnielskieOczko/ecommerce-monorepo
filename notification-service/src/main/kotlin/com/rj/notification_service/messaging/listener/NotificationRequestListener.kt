package com.rj.notification_service.messaging.listener

import com.rj.ecommerce.api.shared.enums.NotificationChannel
import com.rj.ecommerce.api.shared.enums.NotificationDeliveryStatus
import com.rj.ecommerce.api.shared.messaging.notification.common.NotificationDeliveryReceipt
import com.rj.ecommerce.api.shared.messaging.notification.common.NotificationRequest
import com.rj.notification_service.messaging.producer.MessageProducer
import com.rj.notification_service.service.NotificationOrchestrator
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class NotificationRequestListener(
    private val orchestrationService: NotificationOrchestrator,
    private val messageProducer: MessageProducer
) {

    /**
     * Listens to the main email request queue.
     * This is an "at-least-once" delivery mechanism. If processing fails,
     * the message will be rejected and, after retries, sent to the DLQ.
     */
    @RabbitListener(queues = ["\${app.rabbitmq.notificationRequest.queue}"])
    fun handleNotificationRequest(request: NotificationRequest<Any>) {
        val envelope = request.envelope
        logger.info { "Received email request. Correlation ID: ${envelope.correlationId}, Template: ${envelope.template.name}" }

        var status: NotificationDeliveryReceipt? = null
        try {
            orchestrationService.process(request)
            status = NotificationDeliveryReceipt(
                correlationId = envelope.correlationId,
                channel = NotificationChannel.EMAIL, // Specify the channel
                status = NotificationDeliveryStatus.DELIVERED,
                recipientIdentifier = envelope.to
            )
        } catch (e: Exception) {
            status = NotificationDeliveryReceipt(
                correlationId = envelope.correlationId,
                channel = NotificationChannel.EMAIL, // Specify the channel
                status = NotificationDeliveryStatus.FAILED,
                recipientIdentifier = envelope.to,
                errorMessage = e.message ?: "Unknown processing error"
            )
            throw e
        } finally {
            status?.let { messageProducer.sendDeliveryReceipt(it) }
        }
    }
}