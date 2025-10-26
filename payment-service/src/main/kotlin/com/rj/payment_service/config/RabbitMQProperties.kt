package com.rj.payment_service.config

import org.springframework.boot.context.properties.ConfigurationProperties

// The prefix points to the top-level 'app.rabbitmq' key
@ConfigurationProperties(prefix = "app.rabbitmq")
data class RabbitMQProperties(
    // Each property here maps to a nested object in the YAML
    val checkoutSessionRequest: TopicConfig,
    val checkoutSessionResponse: TopicConfig,
    val paymentOptionsRequest: TopicConfig,
    val paymentOptionsReply: TopicConfig
    // You can add a 'dlq' property here if it's also a TopicConfig
) {
    // This nested data class  matches the structure of your YAML
    // (exchange, queue, routing-key)
    data class TopicConfig(
        val exchange: String,
        val queue: String?,
        val routingKey: String
    )
}