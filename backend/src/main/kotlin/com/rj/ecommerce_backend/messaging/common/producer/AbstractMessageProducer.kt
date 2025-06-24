package com.rj.ecommerce_backend.messaging.common.producer

import com.rj.ecommerce_backend.messaging.common.exception.MessagePublishException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.rabbit.core.RabbitTemplate

/**
 * An abstract base class for RabbitMQ message producers.
 *
 * Provides a generic `sendMessage` method that handles message post-processing
 * (for correlation IDs), logging, and exception handling.
 *
 * @param rabbitTemplate The RabbitTemplate instance injected by Spring.
 */
abstract class AbstractMessageProducer(
    // The primary constructor replaces @RequiredArgsConstructor.
    // 'protected val' makes the property protected and final.
    protected val rabbitTemplate: RabbitTemplate
) {

    // The companion object is the standard way to hold a static logger.
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Serializes and sends a message to a specific exchange with a routing key.
     *
     * @param T The type of the message payload.
     * @param exchange The target exchange name.
     * @param routingKey The routing key for the message.
     * @param message The message payload object.
     * @param correlationId An optional correlation ID to attach to the message properties.
     * @throws MessagePublishException if sending the message fails.
     */

    // --- THE FIX IS HERE: <T : Any> ---
    fun <T: Any> sendMessage(exchange: String, routingKey: String, message: T, correlationId: String?) {
        try {
            // Use Kotlin's trailing lambda syntax for the MessagePostProcessor.
            // This is cleaner than creating a separate variable for the lambda.
            rabbitTemplate.convertAndSend(exchange, routingKey, message) { msg ->
                // Use the idiomatic 'isNullOrEmpty()' extension function.
                if (!correlationId.isNullOrEmpty()) {
                    msg.messageProperties.correlationId = correlationId
                }
                msg
            }

            // Use string templates and lambda-based logging for better performance.
            // The string message is only constructed if the log level is enabled.
            logger.info { "Sent message to exchange: $exchange, routing key: $routingKey, correlationId: $correlationId" }
            logger.debug { "Message details: $message" }

        } catch (e: Exception) {
            // kotlin-logging correctly handles passing the exception as the first argument.
            logger.error(e) { "Failed to send message to exchange: $exchange, routing key: $routingKey" }

            // In Kotlin, 'throw' is an expression and doesn't use the 'new' keyword.
            throw MessagePublishException("Failed to publish message", e)
        }
    }
}