package com.rj.payment_service.config

import org.springframework.amqp.core.*
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig(private val props: RabbitMQProperties) {

    // --- Queues, Exchanges, Bindings using properties ---
    @Bean
    fun checkoutSessionExchange(): TopicExchange = TopicExchange(props.checkoutSessionExchange)

    @Bean
    fun checkoutSessionQueue(): Queue = Queue(props.checkoutSessionResponseQueue)

    @Bean
    fun checkoutSessionBinding(): Binding = BindingBuilder.bind(checkoutSessionQueue())
        .to(checkoutSessionExchange()).with(props.checkoutSessionRoutingKey)

    @Bean
    fun checkoutSessionResponseExchange(): TopicExchange = TopicExchange(props.checkoutSessionResponseExchange)

    @Bean
    fun checkoutSessionResponseQueue(): Queue = Queue(props.checkoutSessionResponseQueue)

    @Bean
    fun checkoutSessionResponseBinding(): Binding = BindingBuilder.bind(checkoutSessionResponseQueue())
        .to(checkoutSessionExchange()).with(props.checkoutSessionResponseRoutingKey)

    @Bean
    fun dlqExchange(): TopicExchange = TopicExchange(props.dlqExchange)

    @Bean
    fun dlqQueue(): Queue = Queue(props.dlqQueue)

    @Bean
    fun dlqBinding(): Binding = BindingBuilder.bind(dlqQueue())
        .to(dlqExchange()).with(props.dlqRoutingKey)

    @Bean
    fun jsonMessageConverter(): MessageConverter {
        return Jackson2JsonMessageConverter()
    }
}