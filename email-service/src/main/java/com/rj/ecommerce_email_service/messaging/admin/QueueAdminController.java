package com.rj.ecommerce_email_service.messaging.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rj.ecommerce_email_service.messaging.config.RabbitMQConfig;
import com.rj.ecommerce_email_service.messaging.listener.RabbitMQDeadLetterConfig;

@RestController
@RequestMapping("/admin/queues")
@RequiredArgsConstructor
@Slf4j
public class QueueAdminController {

    private final RabbitAdmin rabbitAdmin;

    @DeleteMapping("/purge/{queueName}")
    public ResponseEntity<String> purgeQueue(@PathVariable String queueName) {
        log.info("Purging queue: {}", queueName);
        
        String actualQueueName;
        
        // Map friendly names to actual queue names
        switch (queueName) {
            case "email":
                actualQueueName = RabbitMQConfig.EMAIL_QUEUE;
                break;
            case "dlq":
                actualQueueName = RabbitMQDeadLetterConfig.EMAIL_DLQ;
                break;
            case "notification":
                actualQueueName = RabbitMQConfig.NOTIFICATION_QUEUE;
                break;
            default:
                actualQueueName = queueName;
        }
        
        rabbitAdmin.purgeQueue(actualQueueName, false);
        
        return ResponseEntity.ok("Queue " + actualQueueName + " has been purged");
    }
    
    @DeleteMapping("/purge-all")
    public ResponseEntity<String> purgeAllQueues() {
        log.info("Purging all queues");
        
        // Purge main queues
        rabbitAdmin.purgeQueue(RabbitMQConfig.EMAIL_QUEUE, false);
        rabbitAdmin.purgeQueue(RabbitMQConfig.NOTIFICATION_QUEUE, false);
        
        // Purge dead letter queue
        rabbitAdmin.purgeQueue(RabbitMQDeadLetterConfig.EMAIL_DLQ, false);
        
        return ResponseEntity.ok("All queues have been purged");
    }
}
