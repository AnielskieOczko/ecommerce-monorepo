package com.rj.payment_service.listener;

import com.rj.payment_service.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeadLetterQueueListener {

    @RabbitListener(queues = RabbitMQConfig.DLQ_QUEUE)
    public void handleDeadLetterQueue(Message message) {
        log.info("Received message from DLQ: {}", message);
        String originalQueue = message
                .getMessageProperties()
                .getHeaders()
                .get("x-original-queue").toString();

        String errorMessage = message
                .getMessageProperties()
                .getHeaders()
                .get("x-original-error-message").toString();

        log.error("Processing failed message from queue: {}", originalQueue);
        log.error("Original error: {}", errorMessage);
        log.error("Message body: {}", new String(message.getBody()));

        // TODO:
        // Here you could:
        // 1. Send notifications to admin
        // 2. Store failed messages in database
        // 3. Implement custom recovery logic
        // 4. Forward to another service
    }
}
