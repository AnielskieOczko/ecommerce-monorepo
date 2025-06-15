package com.rj.ecommerce_backend.messaging.config

import org.springframework.boot.context.properties.ConfigurationProperties

// The prefix "app.rabbitmq" matches the root key in application.yml
@ConfigurationProperties(prefix = "app.rabbitmq")
data class RabbitMQProperties(
    val email: TopicConfig,
    val emailNotification: TopicConfig,
    val checkoutSession: TopicConfig,
    val checkoutSessionResponse: TopicConfig
) {
    // A nested data class for the repeated exchange/queue/routing-key structure
    data class TopicConfig(
        val exchange: String,
        val queue: String,
        val routingKey: String
    )
}