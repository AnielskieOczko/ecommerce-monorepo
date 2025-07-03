package com.rj.ecommerce_backend.messaging.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configures RabbitMQ infrastructure, including exchanges, queues, bindings,
 * and serialization settings. It uses a type-safe RabbitMQProperties class
 * to read topology configuration from application.yml.
 */
@Configuration
@EnableConfigurationProperties
// Note: @EnableConfigurationProperties is typically placed on the main application class,
// but can also be here if you prefer to keep it co-located.
class RabbitMQConfig {

    // --- Core Messaging Infrastructure Beans ---

    /**
     * Defines a specialized ObjectMapper for RabbitMQ messages.
     * This is crucial for handling polymorphism (e.g., sending an interface like
     * EcommerceEmailRequest and having the consumer deserialize it into the correct
     * concrete DTO like OrderEmailRequestDTO). It adds a "@class" property to the JSON.
     */
    @Bean
    @Qualifier("rabbitObjectMapper")
    fun rabbitObjectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

            // Activate default typing to include class information in the JSON payload.
            activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
            )
        }
    }

    /**
     * Creates the MessageConverter that serializes/deserializes messages to/from JSON
     * using our custom rabbitObjectMapper.
     */
    @Bean
    fun jsonMessageConverter(
        @Qualifier("rabbitObjectMapper") rabbitObjectMapper: ObjectMapper
    ): MessageConverter {
        return Jackson2JsonMessageConverter(rabbitObjectMapper)
    }

    /**
     * Configures the RabbitTemplate, which is the primary Spring AMQP tool for sending messages.
     * It's set up to use our custom JSON message converter.
     */
    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        jsonMessageConverter: MessageConverter
    ): RabbitTemplate {
        return RabbitTemplate(connectionFactory).apply {
            messageConverter = jsonMessageConverter
        }
    }


    // --- Topology for Email Sending ---

    @Bean
    fun emailExchange(props: RabbitMQProperties): TopicExchange =
        TopicExchange(props.notificationRequest.exchange, true, false)

    @Bean
    fun emailQueue(props: RabbitMQProperties): Queue =
        Queue(props.notificationRequest.queue, true)

    @Bean
    fun emailBinding(emailQueue: Queue, emailExchange: TopicExchange, props: RabbitMQProperties): Binding =
        BindingBuilder.bind(emailQueue).to(emailExchange).with(props.notificationRequest.routingKey)


    // --- Topology for Email Notification Status ---

    @Bean
    fun emailNotificationExchange(props: RabbitMQProperties): TopicExchange =
        TopicExchange(props.notificationReceipt.exchange, true, false)

    @Bean
    fun emailNotificationQueue(props: RabbitMQProperties): Queue =
        Queue(props.notificationReceipt.queue, true)

    @Bean
    fun emailNotificationBinding(
        emailNotificationQueue: Queue,
        emailNotificationExchange: TopicExchange,
        props: RabbitMQProperties
    ): Binding = BindingBuilder.bind(emailNotificationQueue).to(emailNotificationExchange).with(props.notificationReceipt.routingKey)


    // --- Topology for Checkout Session Creation ---

    @Bean
    fun checkoutSessionExchange(props: RabbitMQProperties): TopicExchange =
        TopicExchange(props.checkoutSession.exchange, true, false)

    @Bean
    fun checkoutSessionQueue(props: RabbitMQProperties): Queue =
        Queue(props.checkoutSession.queue, true)

    @Bean
    fun checkoutSessionBinding(
        checkoutSessionQueue: Queue,
        checkoutSessionExchange: TopicExchange,
        props: RabbitMQProperties
    ): Binding = BindingBuilder.bind(checkoutSessionQueue).to(checkoutSessionExchange).with(props.checkoutSession.routingKey)


    // --- Topology for Checkout Session Responses ---

    @Bean
    fun checkoutSessionResponseExchange(props: RabbitMQProperties): TopicExchange =
        TopicExchange(props.checkoutSessionResponse.exchange, true, false)

    @Bean
    fun checkoutSessionResponseQueue(props: RabbitMQProperties): Queue =
        Queue(props.checkoutSessionResponse.queue, true)

    @Bean
    fun checkoutSessionResponseBinding(
        checkoutSessionResponseQueue: Queue,
        checkoutSessionResponseExchange: TopicExchange,
        props: RabbitMQProperties
    ): Binding = BindingBuilder.bind(checkoutSessionResponseQueue).to(checkoutSessionResponseExchange).with(props.checkoutSessionResponse.routingKey)
}