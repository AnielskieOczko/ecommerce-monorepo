package com.rj.ecommerce_email_service.messaging.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Properties;

import com.rj.ecommerce_email_service.messaging.config.RabbitMQConfig;
import com.rj.ecommerce_email_service.messaging.listener.RabbitMQDeadLetterConfig;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageMonitoringService {

    private final RabbitAdmin rabbitAdmin;
    private final Queue emailQueue;
    private final Queue deadLetterQueue;

    /**
     * Scheduled task to monitor queue depths
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    public void monitorQueues() {
        Properties emailQueueProps = rabbitAdmin.getQueueProperties(RabbitMQConfig.EMAIL_QUEUE);
        Properties dlqProps = rabbitAdmin.getQueueProperties(RabbitMQDeadLetterConfig.EMAIL_DLQ);
        
        if (emailQueueProps != null) {
            Integer messageCount = (Integer) emailQueueProps.get(RabbitAdmin.QUEUE_MESSAGE_COUNT);
            log.info("Current email queue depth: {}", messageCount);
        }
        
        if (dlqProps != null) {
            Integer dlqMessageCount = (Integer) dlqProps.get(RabbitAdmin.QUEUE_MESSAGE_COUNT);
            log.info("Current dead letter queue depth: {}", dlqMessageCount);
            
            // Alert if DLQ has messages
            if (dlqMessageCount != null && dlqMessageCount > 0) {
                log.warn("ALERT: Dead letter queue has {} messages that require attention", dlqMessageCount);
            }
        }
    }
}
