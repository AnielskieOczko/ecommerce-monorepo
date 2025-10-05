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
@EnableConfigurationProperties
class RabbitMQConfig {

    // The duplicate rabbitObjectMapper bean has been REMOVED from this file.
    // We now inject the one defined in JacksonConfig.

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