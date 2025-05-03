package com.rj.payment_service.producer;

import com.rj.payment_service.exception.MessagePublishException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.rj.payment_service.config.RabbitMQConfig.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class RabbitMQProducer {
    private final RabbitTemplate rabbitTemplate;

    public <T> void sendMessage(String exchange, String routingKey, T message, String correlationId) {

        try {
            MessagePostProcessor messagePostProcessor = msg -> {
                if (correlationId != null && !correlationId.isEmpty()) {
                    msg.getMessageProperties().setCorrelationId(correlationId);
                }
                return msg;
            };

            rabbitTemplate.convertAndSend(exchange, routingKey, message, messagePostProcessor);
            log.info("Sent message to exchange: {}, routing key: {}, message: {}", exchange, routingKey, message);
        } catch (Exception e) {
            log.error("Failed to send message to exchange: {}, routing key: {}, message: {}", exchange, routingKey, message, e);
            throw new MessagePublishException("Failed to publish message", e);
        }

    }

    public <T> void sendCheckoutSessionResponse(T response, String correlationId) {
        sendMessage(
                CHECKOUT_SESSION_RESPONSE_EXCHANGE,
                CHECKOUT_SESSION_RESPONSE_ROUTING_KEY,
                response,
                correlationId
        );
    }


}
