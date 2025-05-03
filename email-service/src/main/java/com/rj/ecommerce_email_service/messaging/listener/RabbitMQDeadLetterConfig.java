package com.rj.ecommerce_email_service.messaging.listener;

import com.rj.ecommerce_email_service.messaging.config.RabbitMQConfig;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQDeadLetterConfig {
    
    public static final String EMAIL_DLX = "email.dlx";
    public static final String EMAIL_DLQ = "email.dlq";
    public static final String EMAIL_DL_ROUTING_KEY = "email.dead.letter.key";

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(EMAIL_DLX);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(EMAIL_DLQ).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(EMAIL_DL_ROUTING_KEY);
    }

    /**
     * Reconfigure the main email queue to use dead letter exchange
     */
    @Bean
    public Queue emailQueueWithDLX() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", EMAIL_DLX);
        args.put("x-dead-letter-routing-key", EMAIL_DL_ROUTING_KEY);
        // Set message TTL to 10 seconds before moving to DLQ
        args.put("x-message-ttl", 10000);
        // Maximum number of retries
        args.put("x-max-retries", 3);
        
        return QueueBuilder.durable(RabbitMQConfig.EMAIL_QUEUE)
                .withArguments(args)
                .build();
    }
}
