package com.rj.payment_service.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.rabbitmq")
data class RabbitMQProperties(
    val checkoutSessionQueue: String,
    val checkoutSessionResponseQueue: String,
    val checkoutSessionExchange: String,
    val checkoutSessionRoutingKey: String,
    val checkoutSessionResponseExchange: String,
    val checkoutSessionResponseRoutingKey: String,
    val dlqQueue: String,
    val dlqExchange: String,
    val dlqRoutingKey: String
)