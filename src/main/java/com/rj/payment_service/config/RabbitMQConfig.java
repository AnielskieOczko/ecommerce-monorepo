package com.rj.payment_service.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
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

@Configuration
@Slf4j
public class RabbitMQConfig {

    // Dead Letter Queue
    public static final String DLQ_QUEUE = "payment-dlq-queue";
    public static final String DLQ_EXCHANGE = "payment-dlq-exchange";
    public static final String DLQ_ROUTING_KEY = "payment-dlq-routing-key";

    // Payment Verification Response
    public static final String CHECKOUT_SESSION_RESPONSE_QUEUE = "checkout-session-response-queue";
    public static final String CHECKOUT_SESSION_RESPONSE_EXCHANGE = "checkout-session-response-exchange";
    public static final String CHECKOUT_SESSION_RESPONSE_ROUTING_KEY = "checkout-session-response-routing-key";

    // Payment Verification Response
    public static final String CHECKOUT_SESSION_QUEUE = "checkout-session-queue";
    public static final String CHECKOUT_SESSION_EXCHANGE = "checkout-session-exchange";
    public static final String CHECKOUT_SESSION_ROUTING_KEY = "checkout-session-routing-key";


    @Bean
    public TopicExchange checkoutSessionExchange() {
        return new TopicExchange(CHECKOUT_SESSION_EXCHANGE, true, false);
    }

    @Bean
    public Queue checkoutSessionQueue() {
        return new Queue(CHECKOUT_SESSION_QUEUE, true);
    }

    @Bean
    public Binding checkoutSessionBinding() {
        return BindingBuilder
                .bind(checkoutSessionQueue())
                .to(checkoutSessionExchange())
                .with(CHECKOUT_SESSION_ROUTING_KEY);
    }

    @Bean
    public TopicExchange checkoutSessionResponseExchange() {
        return new TopicExchange(CHECKOUT_SESSION_RESPONSE_EXCHANGE, true, false);
    }

    @Bean
    public Queue checkoutSessionResponseQueue() {
        return new Queue(CHECKOUT_SESSION_RESPONSE_QUEUE, true);
    }

    @Bean
    public Binding checkoutSessionResponseBinding() {
        return BindingBuilder
                .bind(checkoutSessionResponseQueue())
                .to(checkoutSessionResponseExchange())
                .with(CHECKOUT_SESSION_RESPONSE_ROUTING_KEY);
    }


    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLQ_EXCHANGE, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DLQ_QUEUE, true);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DLQ_ROUTING_KEY);
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

        return objectMapper;
    }

    @Bean
    MessageConverter jsonMessageConverter(ObjectMapper rabbitObjectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(rabbitObjectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();

        final String PRODUCER_DTO_CLASS = "com.rj.ecommerce_backend.messaging.payment.dto.CheckoutSessionRequestDTO";

        idClassMapping.put(
                PRODUCER_DTO_CLASS,
                com.rj.payment_service.dto.request.CheckoutSessionRequestDTO.class
        );

        // You might need to map the nested class too if the producer is sending type info for it
        // (Depends on how Jackson handles nested records with NON_FINAL)
        idClassMapping.put(
                PRODUCER_DTO_CLASS + "$CheckoutLineItemDTO", // Producer Nested
                com.rj.payment_service.dto.request.CheckoutSessionRequestDTO.CheckoutLineItemDTO.class
        );

        typeMapper.setIdClassMapping(idClassMapping);

        // *** CORRECTED Trusted Packages ***
        // Add the producer's package name here accurately
        final String PRODUCER_DTO_PACKAGE = "com.rj.ecommerce_backend.messaging.payment.dto";

        typeMapper.addTrustedPackages(
                PRODUCER_DTO_PACKAGE,                             // Producer's package
                "com.rj.payment_service.dto.request",           // Consumer's package
                "java.util",
                "java.time"
        );
        converter.setClassMapper(typeMapper);
        return converter;
    }

}
