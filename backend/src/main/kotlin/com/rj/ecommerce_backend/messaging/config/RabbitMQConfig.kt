package com.rj.ecommerce_backend.messaging.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
// This annotation tells Spring to find and create a bean for RabbitMQProperties
@EnableConfigurationProperties(RabbitMQProperties::class)
class RabbitMQConfig(
    // Now Spring can inject the RabbitMQProperties bean into our constructor
    private val props: RabbitMQProperties
) {

    @Bean
    fun jsonMessageConverter(
        @Qualifier("rabbitObjectMapper") rabbitObjectMapper: ObjectMapper
    ): MessageConverter {
        return Jackson2JsonMessageConverter(rabbitObjectMapper)
    }

    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        jsonMessageConverter: MessageConverter
    ): RabbitTemplate {
        return RabbitTemplate(connectionFactory).apply {
            messageConverter = jsonMessageConverter
        }
    }

    // --- Topology for Email Sending (FIXED) ---
    @Bean
    fun emailExchange(): TopicExchange =
        TopicExchange(props.notificationRequest.exchange, true, false)

    @Bean
    fun emailQueue(): Queue {
        // Corrected declaration with QueueBuilder and hardcoded DLQ values.
        // Since RabbitMQProperties doesn't contain DLQ info, we add it directly.
        return QueueBuilder.durable(props.notificationRequest.queue)
            .withArgument("x-dead-letter-exchange", "notification.request.dlx")
            .withArgument("x-dead-letter-routing-key", "notification.request.dlq")
            .build()
    }

    @Bean
    fun emailBinding(emailQueue: Queue, emailExchange: TopicExchange): Binding =
        BindingBuilder.bind(emailQueue).to(emailExchange).with(props.notificationRequest.routingKey)

    // --- Topology for Email Notification Status ---
    @Bean
    fun emailNotificationExchange(): TopicExchange =
        TopicExchange(props.notificationReceipt.exchange, true, false)

    @Bean
    fun emailNotificationQueue(): Queue =
        Queue(props.notificationReceipt.queue, true)

    @Bean
    fun emailNotificationBinding(
        emailNotificationQueue: Queue,
        emailNotificationExchange: TopicExchange
    ): Binding = BindingBuilder.bind(emailNotificationQueue).to(emailNotificationExchange).with(props.notificationReceipt.routingKey)

    // --- Topology for Checkout Session Creation ---
    @Bean
    fun checkoutSessionExchange(): TopicExchange =
        TopicExchange(props.checkoutSession.exchange, true, false)

    @Bean
    fun checkoutSessionQueue(): Queue =
        Queue(props.checkoutSession.queue, true)

    @Bean
    fun checkoutSessionBinding(
        checkoutSessionQueue: Queue,
        checkoutSessionExchange: TopicExchange
    ): Binding = BindingBuilder.bind(checkoutSessionQueue).to(checkoutSessionExchange).with(props.checkoutSession.routingKey)

    // --- Topology for Checkout Session Responses ---
    @Bean
    fun checkoutSessionResponseExchange(): TopicExchange =
        TopicExchange(props.checkoutSessionResponse.exchange, true, false)

    @Bean
    fun checkoutSessionResponseQueue(): Queue =
        Queue(props.checkoutSessionResponse.queue, true)

    @Bean
    fun checkoutSessionResponseBinding(
        checkoutSessionResponseQueue: Queue,
        checkoutSessionResponseExchange: TopicExchange
    ): Binding = BindingBuilder.bind(checkoutSessionResponseQueue).to(checkoutSessionResponseExchange).with(props.checkoutSessionResponse.routingKey)
}