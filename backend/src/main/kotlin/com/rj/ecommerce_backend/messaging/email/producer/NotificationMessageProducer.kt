package com.rj.ecommerce_backend.messaging.email.producer

import com.rj.ecommerce.api.shared.messaging.notification.common.NotificationRequest
import com.rj.ecommerce_backend.messaging.common.producer.AbstractMessageProducer
import com.rj.ecommerce_backend.messaging.config.RabbitMQProperties
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class NotificationMessageProducer(
    rabbitTemplate: RabbitTemplate,
    private val rabbitMQProperties: RabbitMQProperties
) : AbstractMessageProducer(rabbitTemplate) {

    /**
     * Sends a generic, composed notification request to the notification-service.
     */
    fun send(request: NotificationRequest<*>) {
        // Note: We are reusing the 'email' queue configuration name from the backend's properties.
        // This should be renamed to 'notificationRequest' in the backend's application.yml for clarity.
        val notificationConfig = rabbitMQProperties.notificationRequest
        sendMessage(
            exchange = notificationConfig.exchange,
            routingKey = notificationConfig.routingKey,
            message = request,
            correlationId = request.envelope.correlationId
        )
    }
}