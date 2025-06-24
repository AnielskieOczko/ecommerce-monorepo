package producer

import com.rj.payment_service.exception.MessagePublishException
import config.RabbitMQProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class MessageProducer(
    private val rabbitTemplate: RabbitTemplate,
    private val rabbitMQProperties: RabbitMQProperties
) {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    fun <T : Any> sendMessage(exchange: String, routingKey: String, message: T, correlationId: String?) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message) { msg ->
                if (!correlationId.isNullOrEmpty()) {
                    msg.messageProperties.correlationId = correlationId
                }
                msg
            }

            logger.info { "Sent message to exchange: $exchange, routing key: $routingKey, correlationId: $correlationId" }
            logger.debug { "Message details: $message" }

        } catch (e: Exception) {
            logger.error(e) { "Failed to send message to exchange: $exchange, routing key: $routingKey" }
            throw MessagePublishException("Failed to publish message", e)
        }
    }

    fun <T : Any> sendCheckoutSessionResponse(response: T, correlationId: String) {
        sendMessage(
            exchange = rabbitMQProperties.checkoutSessionExchange,
            routingKey = rabbitMQProperties.checkoutSessionResponseRoutingKey,
            message = response,
            correlationId = correlationId

        )
    }
}