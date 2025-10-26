package com.rj.payment_service.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig(private val props: RabbitMQProperties) {

    // --- Topology for INCOMING Checkout Session Requests ---
    // This service LISTENS for requests, so it needs to declare the queue and bind it.

    @Bean
    fun checkoutSessionRequestExchange(): TopicExchange =
        TopicExchange(props.checkoutSessionRequest.exchange)

    @Bean
    fun checkoutSessionRequestQueue(): Queue {
        // We use !! because if this queue name is null, the service cannot function.
        // It's better to fail fast at startup.
        return Queue(props.checkoutSessionRequest.queue!!)
    }

    @Bean
    fun checkoutSessionRequestBinding(
        checkoutSessionRequestQueue: Queue,
        checkoutSessionRequestExchange: TopicExchange
    ): Binding =
        BindingBuilder.bind(checkoutSessionRequestQueue)
            .to(checkoutSessionRequestExchange)
            .with(props.checkoutSessionRequest.routingKey)

    // --- Topology for OUTGOING Checkout Session Responses ---
    // This service SENDS responses, so it only needs to declare the exchange it sends to.
    // It does NOT need to know about the queue or the binding on the other side.

    @Bean
    fun checkoutSessionResponseExchange(): TopicExchange =
        TopicExchange(props.checkoutSessionResponse.exchange)

    // --- Topology for Payment Options (Request/Reply) ---
    // This service LISTENS for requests, so it declares the request queue and binding.
    @Bean
    fun paymentOptionsRequestExchange(): TopicExchange =
        TopicExchange(props.paymentOptionsRequest.exchange)

    @Bean
    fun paymentOptionsRequestQueue(): Queue =
        Queue(props.paymentOptionsRequest.queue!!)

    @Bean
    fun paymentOptionsRequestBinding(
        paymentOptionsRequestQueue: Queue,
        paymentOptionsRequestExchange: TopicExchange
    ): Binding =
        BindingBuilder.bind(paymentOptionsRequestQueue)
            .to(paymentOptionsRequestExchange)
            .with(props.paymentOptionsRequest.routingKey)

    // This service SENDS replies, so it only needs the reply exchange. The producer will
    // use the routing key from the properties. No queue or binding needed here.
    @Bean
    fun paymentOptionsReplyExchange(): TopicExchange =
        TopicExchange(props.paymentOptionsReply.exchange)

    // --- Message Converter ---
    // Use the same shared Jackson configuration as the backend for consistency.
    @Bean
    fun jsonMessageConverter(
        @Qualifier("rabbitObjectMapper") rabbitObjectMapper: ObjectMapper
    ): MessageConverter {
        return Jackson2JsonMessageConverter(rabbitObjectMapper)
    }

    // You also need the rabbitObjectMapper bean itself in a JacksonConfig file, just like in the backend.
}