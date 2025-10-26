package com.rj.ecommerce_backend.api.shared.messaging.notification.common

import com.rj.ecommerce.api.shared.enums.NotificationChannel
import com.rj.ecommerce.api.shared.enums.NotificationTemplate
import com.rj.ecommerce.api.shared.messaging.contract.MessageVersioning
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

@Schema(description = "Holds the metadata for a notification, defining the recipient, channels, and template.")
data class NotificationEnvelope(
    @field:Schema(description = "The recipient's address (e.g., email address or phone number).", required = true)
    val to: String,

    @field:Schema(description = "The subject line, primarily for email channels.", required = true)
    val subject: String,

    @field:Schema(description = "The template to be used for rendering the notification content.", required = true)
    val template: NotificationTemplate,

    @field:Schema(description = "The set of channels (e.g., EMAIL, SMS) to send this notification through.", required = true)
    val channels: Set<NotificationChannel>,

    @field:Schema(description = "A unique ID to trace the entire notification lifecycle across services.", required = true)
    val correlationId: String,

    @field:Schema(description = "The unique ID of this specific message.")
    val messageId: String = UUID.randomUUID().toString(),

    @field:Schema(description = "The version of this message contract.")
    val version: String = MessageVersioning.CURRENT_VERSION,

    @field:Schema(description = "The timestamp when the message was created.")
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(channels.isNotEmpty()) { "Notification channels set cannot be empty." }
        require(correlationId.isNotBlank()) { "Correlation ID cannot be blank." }
    }
}