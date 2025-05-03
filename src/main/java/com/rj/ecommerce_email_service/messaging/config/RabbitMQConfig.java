package com.rj.ecommerce_email_service.messaging.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.databind.util.ClassUtil.classOf;

@Configuration
public class RabbitMQConfig {
    public static final String EMAIL_EXCHANGE = "email.exchange";
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String ROUTING_KEY = "email.routing.key";

    // Email notification queue (for sending status back)
    public static final String NOTIFICATION_EXCHANGE = "email.notification.exchange";
    public static final String NOTIFICATION_QUEUE = "email.notification.queue";
    public static final String NOTIFICATION_ROUTING_KEY = "email.notification.routing.key";

    @Bean
    public TopicExchange emailExchange() {
        return new TopicExchange(EMAIL_EXCHANGE, true, false);
    }

    @Bean
    public Queue emailQueue() {
        return new Queue(EMAIL_QUEUE, true);
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue())
                .to(emailExchange())
                .with(ROUTING_KEY);
    }

    @Bean
    TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }


    // Define the ObjectMapper bean here too for consistency
    @Bean
    public ObjectMapper rabbitObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        // Consumer might need FAIL_ON_UNKNOWN_PROPERTIES disabled if DTOs diverge slightly
        // objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }



    /**
     * Configures a custom Jackson2JsonMessageConverter for RabbitMQ that enables cross-package deserialization.
     * This configuration solves the issue of deserializing messages sent from external systems with different
     * package structures. Specifically, it maps class names from the main application's package structure
     * (com.rj.ecommerce_backend.messaging.email.contract.v1.*) to the email service's package structure
     * (com.rj.ecommerce_email_service.contract.v1.*).
     * <p>
     * Without this configuration, RabbitMQ would attempt to deserialize messages into classes with the
     * exact package name specified in the __TypeId__ header, resulting in ClassNotFoundException errors
     * when the packages don't match between sender and receiver.
     *
     * @return A configured Jackson2JsonMessageConverter that can handle cross-package deserialization
     */
    @Bean
    MessageConverter jsonMessageConverter(ObjectMapper rabbitObjectMapper) { // Inject the ObjectMapper
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(rabbitObjectMapper); // Use the configured ObjectMapper

        // --- Configure Type Mapping ---
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();

        // Add your mappings
        idClassMapping.put(
                "com.rj.ecommerce_backend.messaging.email.contract.v1.order.OrderEmailRequestDTO",
                com.rj.ecommerce_email_service.contract.v1.order.OrderEmailRequestDTO.class
        );
        idClassMapping.put(
                "com.rj.ecommerce_backend.messaging.email.contract.v1.payment.PaymentEmailRequestDTO",
                com.rj.ecommerce_email_service.contract.v1.payment.PaymentEmailRequestDTO.class
        );
        // *** IMPORTANT: Also map nested DTOs if they are sent polymorphically or could cause issues ***
        // Although NON_FINAL typing *should* handle nested types correctly now by embedding @class,
        // explicit mapping can sometimes help resolve ambiguities if needed. Usually not required for nested records/beans.
       /*
       idClassMapping.put(
                "com.rj.ecommerce_backend.messaging.email.contract.v1.order.OrderItemDTO",
                com.rj.ecommerce_email_service.contract.v1.order.OrderItemDTO.class
       );
       idClassMapping.put(
                "com.rj.ecommerce_backend.messaging.email.contract.v1.common.MoneyDTO",
                com.rj.ecommerce_email_service.contract.v1.common.MoneyDTO.class
       );
       // etc. for CustomerDTO, AddressDTO if necessary
       */

        typeMapper.setIdClassMapping(idClassMapping);

        // Trust packages from both producer and consumer contracts
        typeMapper.addTrustedPackages(
                "com.rj.ecommerce_backend.messaging.email.contract.v1",
                "com.rj.ecommerce_email_service.contract.v1",
                "java.util", // Trust standard Java collections
                "java.time"  // Trust Java time types
        );
        converter.setClassMapper(typeMapper);
        // --- End Type Mapping Config ---

        return converter;
    }


}
