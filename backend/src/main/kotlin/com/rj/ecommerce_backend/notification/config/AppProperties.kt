package com.rj.ecommerce_backend.notification.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Main application configuration properties.
 *
 * Properties are nullable where optional to allow partial configuration.
 * Spring Boot 3.x uses constructor binding for data classes automatically.
 */
@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val notification: NotificationConfig,
) {

    data class NotificationConfig(
        val channels: Map<String, ChannelConfig>,
        val vendors: VendorConfig
    )

    data class ChannelConfig(
        val activeProvider: String,
        val defaultFrom: String
    )

    data class VendorConfig(
        val smtp: SmtpConfig? = null,
        val sendgrid: SendGridConfig? = null,
        val twilio: TwilioConfig? = null
    )

    data class SmtpConfig(
        val host: String,
        val port: Int = 587,
        val username: String? = null,
        val password: String? = null,
        val protocol: String = "smtp",
        val properties: Map<String, String> = emptyMap()
    )

    // Make properties nullable since they may not be configured
    data class SendGridConfig(
        val apiKey: String?
    )

    data class TwilioConfig(
        val accountSid: String?,
        val authToken: String?
    )

    data class MonitoringProperties(
        val enabled: Boolean = true,
        val schedule: String = "0 */1 * * * *"
    )
}