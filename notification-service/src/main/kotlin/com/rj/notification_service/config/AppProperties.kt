package com.rj.notification_service.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val rabbitmq: RabbitMQProperties,
    val notification: NotificationConfig,
    val monitoring: MonitoringProperties,
) {
    data class RabbitMQProperties(
        val notificationRequest: TopicConfig,
        val notificationReceipt: TopicConfig
    )

    data class TopicConfig(
        val exchange: String,
        val queue: String,
        val routingKey: String,
        // NEW: Add an optional nested config for the DLQ
        val dlq: DlqConfig?
    )

    data class DlqConfig(
        val exchange: String,
        val routingKey: String
        // The DLQ name is derived from the main queue name (e.g., "email.queue.dlq")
    )

    data class NotificationConfig(
        val channels: Map<String, ChannelConfig>,
        val vendors: VendorConfig
    )

    data class ChannelConfig(
        val activeProvider: String,
        val defaultFrom: String
    )

    data class VendorConfig(
        val sendgrid: SendGridConfig?,
        val twilio: TwilioConfig?
    )

    data class SmtpConfig(
        val host: String,
        val port: Int = 587,
        val username: String?,
        val password: String?,
        val protocol: String = "smtp",
        val properties: Map<String, String> = emptyMap() // For mail.smtp.* properties
    )

    data class SendGridConfig(val apiKey: String)
    data class TwilioConfig(val accountSid: String, val authToken: String)

    data class MonitoringProperties(
        val enabled: Boolean = true,
        val schedule: String = "0 */1 * * * *" // Every minute
    )
}