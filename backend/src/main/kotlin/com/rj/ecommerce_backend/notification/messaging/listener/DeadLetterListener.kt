package com.rj.ecommerce_backend.notification.messaging.listener

import com.rabbitmq.client.Channel
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger { DeadLetterListener::class }

@Component
class DeadLetterListener {


    /**
     * Listens to the email request Dead-Letter Queue.
     * Messages end up here after all retry attempts have failed.
     */
    @RabbitListener(queues = ["\${app.rabbitmq.notificationRequest.queue}.dlq"])
    fun handleDeadLetter(message: Message, channel: Channel) {
        val correlationId = message.messageProperties.correlationId ?: "UNKNOWN"
        logger.error {
            """
            ==================== DEAD LETTER RECEIVED ====================
            Message failed all processing attempts. Manual intervention may be required.
            Correlation ID: $correlationId
            Exchange: ${message.messageProperties.receivedExchange}
            Routing Key: ${message.messageProperties.receivedRoutingKey}
            Headers: ${message.messageProperties.headers}
            Body: ${String(message.body)}
            ==============================================================
            """.trimIndent()
        }
        // We could optionally publish this error to another topic for alerting,
        // or save it to a database for analysis. For now, logging is sufficient.
    }
}