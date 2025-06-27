package com.rj.ecommerce_backend.messaging.email.producer

import com.rj.ecommerce.api.shared.messaging.email.EmailRequest
import com.rj.ecommerce_backend.messaging.common.producer.AbstractMessageProducer
import com.rj.ecommerce_backend.messaging.config.RabbitMQProperties
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class EmailMessageProducer(
    rabbitTemplate: RabbitTemplate,
    private val rabbitMQProperties: RabbitMQProperties
) : AbstractMessageProducer(rabbitTemplate) {

    /**
     * Sends a generic, composed email request to the email exchange.
     */
    fun send(request: EmailRequest<*>) {
        val emailConfig = rabbitMQProperties.email
        sendMessage(
            exchange = emailConfig.exchange,
            routingKey = emailConfig.routingKey,
            message = request,
            correlationId = request.envelope.correlationId
        )
    }
}