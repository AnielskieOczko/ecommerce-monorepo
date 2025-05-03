package com.rj.ecommerce_email_service.messaging.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeadLetterQueueListener {

    @RabbitListener(queues = RabbitMQDeadLetterConfig.EMAIL_DLQ)
    public void processFailedMessages(Message message) {
        log.error("Received dead letter message: {}", message);
        
        // Extract message headers to understand why it failed
        String xDeathHeader = message.getMessageProperties().getHeader("x-death") != null ? 
                message.getMessageProperties().getHeader("x-death").toString() : "No x-death header";
        
        log.error("Message failure reason: {}", xDeathHeader);
        
        // Here you could implement:
        // 1. Notification to admin
        // 2. Store failed messages in database for later analysis
        // 3. Attempt to fix and republish if possible
    }
}
