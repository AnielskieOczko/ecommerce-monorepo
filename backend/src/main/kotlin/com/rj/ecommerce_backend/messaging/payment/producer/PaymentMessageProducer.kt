package com.rj.ecommerce_backend.messaging.payment.producer

import com.rj.ecommerce_backend.messaging.common.producer.AbstractMessageProducer
import com.rj.ecommerce_backend.messaging.config.RabbitMQProperties
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

/**
 * A specialized message producer for sending payment-related requests,
 * such as initiating a checkout session.
 *
 * It extends the generic AbstractMessageProducer and is pre-configured
 * with the correct exchange and routing key for checkout session requests.
 */
@Component
class PaymentMessageProducer(
    rabbitTemplate: RabbitTemplate,
    private val rabbitMQProperties: RabbitMQProperties
) : AbstractMessageProducer(rabbitTemplate) {

    /**
     * Sends a generic checkout session request to the appropriate queue.
     * The use of a generic type <T> allows this method to send different kinds
     * of payment request DTOs if needed in the future, without modification.
     *
     * @param T The non-nullable type of the request payload.
     * @param request The payment request DTO to be sent.
     * @param correlationId A unique identifier for tracking the message.
     */
    fun <T : Any> sendCheckoutSessionRequest(request: T, correlationId: String) {
        val checkoutSessionConfig = rabbitMQProperties.checkoutSession

        sendMessage(
            exchange = checkoutSessionConfig.exchange,
            routingKey = checkoutSessionConfig.routingKey,
            message = request,
            correlationId = correlationId
        )
    }
}