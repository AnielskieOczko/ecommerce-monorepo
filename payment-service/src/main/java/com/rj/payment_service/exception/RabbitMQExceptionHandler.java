package com.rj.payment_service.exception;

import com.stripe.exception.StripeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RabbitMQExceptionHandler {


    @EventListener
    public void handleListenerExecutionFailedException(ListenerExecutionFailedException event) {
        Message failedMessage = event.getFailedMessage();
        Throwable throwable = event.getCause();
        String correlationId = failedMessage.getMessageProperties().getCorrelationId();

        if (throwable instanceof PaymentValidationException) {
            log.warn("Validation error for message (correlationId: {}): {}",
                    correlationId, throwable.getMessage());
            throw new AmqpRejectAndDontRequeueException(throwable);

        } else if (throwable instanceof PaymentProcessingException) {
            log.error("Processing error for message (correlationId: {}): {}",
                    correlationId, throwable.getMessage());
            // Allow requeue for retry
            throw (PaymentProcessingException) throwable;

        } else if (throwable instanceof MessageConversionException) {
            log.error("Message conversion error (correlationId: {}): {}",
                    correlationId, throwable.getMessage());
            throw new AmqpRejectAndDontRequeueException(throwable);
        }

        log.error("Unhandled error for message (correlationId: {})", correlationId, throwable);
        throw new AmqpRejectAndDontRequeueException("Error processing RabbitMQ message", throwable);
    }


}
