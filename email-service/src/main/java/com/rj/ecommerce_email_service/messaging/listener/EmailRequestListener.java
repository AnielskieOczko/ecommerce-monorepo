package com.rj.ecommerce_email_service.messaging.listener;

import com.rj.ecommerce_email_service.contract.v1.EcommerceEmailRequest;
import com.rj.ecommerce_email_service.contract.v1.notification.EmailDeliveryStatusDTO;
import com.rj.ecommerce_email_service.messaging.producer.EmailMessageProducer;
import com.rj.ecommerce_email_service.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.rj.ecommerce_email_service.messaging.config.RabbitMQConfig.EMAIL_QUEUE;
import static com.rj.ecommerce_email_service.messaging.config.RabbitMQConfig.NOTIFICATION_EXCHANGE;
import static com.rj.ecommerce_email_service.messaging.config.RabbitMQConfig.NOTIFICATION_ROUTING_KEY;

/**
 * Listener for email requests from RabbitMQ.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailRequestListener {
    
    private final EmailService emailService;
    private final EmailMessageProducer emailMessageProducer;
    
    @RabbitListener(queues = EMAIL_QUEUE)
    public void processEmailRequest(EcommerceEmailRequest request) {
        log.info("Received email request: {}", request.getMessageId());
        
        // Process the email
        EmailDeliveryStatusDTO status = emailService.processEmail(request);

        // Send status notification
        emailMessageProducer.sendEmail(NOTIFICATION_EXCHANGE, NOTIFICATION_ROUTING_KEY, status, request.getMessageId());
        
        log.info("Email processing completed with status: {}", status.status());
    }
}
