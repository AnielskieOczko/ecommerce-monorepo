package com.rj.ecommerce_backend.messaging.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.rabbitmq")
data class RabbitMQProperties(
    val email: TopicConfig,
    val emailNotification: TopicConfig,
    val checkoutSession: TopicConfig,
    val checkoutSessionResponse: TopicConfig,
    val paymentOptionsRequest: TopicConfig,
    val paymentOptionsReply: TopicConfig
) {
    // A nested data class for the repeated exchange/queue/routing-key structure
    data class TopicConfig(
        val exchange: String,
        val queue: String,
        val routingKey: String
    )
}