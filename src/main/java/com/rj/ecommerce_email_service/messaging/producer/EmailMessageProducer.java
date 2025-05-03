package com.rj.ecommerce_email_service.messaging.producer;

import com.rj.ecommerce_email_service.messaging.common.AbstractMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailMessageProducer extends AbstractMessageProducer {


    public EmailMessageProducer(RabbitTemplate rabbitTemplate) {
        super(rabbitTemplate);
    }

    public void sendEmail(String exchange, String routingKey, Object message, String correlationId) {
        sendMessage(exchange, routingKey, message, correlationId);
    }
}
