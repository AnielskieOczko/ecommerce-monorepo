package com.rj.ecommerce_backend.payment.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException
import org.springframework.amqp.support.converter.MessageConversionException
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class RabbitMQExceptionHandler {

    @EventListener
    fun handleListenerExecutionFailed(event: ListenerExecutionFailedException) {
        val failedMessage = event.failedMessage
        val cause = event.cause
        val correlationId = failedMessage.messageProperties.correlationId

        when (cause) {
            is PaymentValidationException -> {
                logger.warn(cause) { "Validation error for message (correlationId: $correlationId): ${cause.message}" }
                // Do not requeue, send to DLQ
                throw AmqpRejectAndDontRequeueException(cause)
            }
            is PaymentProcessingException -> {
                logger.error(cause) { "Processing error for message (correlationId: $correlationId): ${cause.message}" }
                // Allow requeue for retry by re-throwing the original exception
                throw cause
            }
            is MessageConversionException -> {
                logger.error(cause) { "Message conversion error (correlationId: $correlationId): ${cause.message}" }
                // Do not requeue, send to DLQ
                throw AmqpRejectAndDontRequeueException(cause)
            }
            else -> {
                logger.error(cause) { "Unhandled error for message (correlationId: $correlationId)" }
                // Do not requeue, send to DLQ
                throw AmqpRejectAndDontRequeueException("Unhandled error processing RabbitMQ message", cause)
            }
        }
    }
}