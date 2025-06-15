package com.rj.ecommerce_backend.messaging.email.producer

import com.rj.ecommerce.api.shared.messaging.email.EcommerceEmailRequest
import com.rj.ecommerce_backend.messaging.common.producer.AbstractMessageProducer
import com.rj.ecommerce_backend.messaging.config.RabbitMQProperties
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

/**
 * A specialized message producer for sending email requests.
 *
 * This class extends the generic AbstractMessageProducer and is pre-configured
 * with the correct exchange and routing key for email messages, read from
 * the application configuration.
 */
@Component
class EmailMessageProducer(
    // Dependencies are injected via the primary constructor
    rabbitTemplate: RabbitTemplate,
    private val rabbitMQProperties: RabbitMQProperties // Inject the type-safe properties
) : AbstractMessageProducer(rabbitTemplate) { // Call to the superclass constructor

    /**
     * Sends a structured email request to the email exchange.
     * The correlationId is typically the unique message ID of the request itself.
     *
     * @param request The email request DTO (e.g., OrderEmailRequestDTO, PaymentEmailRequestDTO).
     * @param correlationId A unique identifier for tracking the message, often the messageId.
     */
    fun sendEmail(request: EcommerceEmailRequest, correlationId: String) {
        // Use the properties object to get the exchange and routing key.
        // This is safer and more flexible than using hardcoded constants.
        val emailConfig = rabbitMQProperties.email

        sendMessage(
            exchange = emailConfig.exchange,
            routingKey = emailConfig.routingKey,
            message = request,
            correlationId = correlationId
        )
    }
}