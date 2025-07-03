package com.rj.ecommerce.api.shared.messaging.email

import com.rj.ecommerce.api.shared.enums.NotificationChannel
import com.rj.ecommerce.api.shared.enums.NotificationTemplate
import com.rj.ecommerce.api.shared.messaging.contract.MessageVersioning
import java.time.LocalDateTime
import java.util.UUID

/**
 * Holds the metadata common to all email messages.
 */
/**
 * Holds the metadata common to all notification messages.
 */
data class NotificationEnvelope(
    val to: String,
    val subject: String, // May be ignored by non-email channels
    val template: NotificationTemplate,
    val channels: Set<NotificationChannel>, // CHANGED: Now a Set to support multiple channels
    val correlationId: String,
    val messageId: String = UUID.randomUUID().toString(),
    val version: String = MessageVersioning.CURRENT_VERSION,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(channels.isNotEmpty()) { "Notification channels set cannot be empty." }
    }
}