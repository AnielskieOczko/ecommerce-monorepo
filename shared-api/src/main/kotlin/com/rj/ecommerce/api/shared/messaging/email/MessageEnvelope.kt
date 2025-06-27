package com.rj.ecommerce.api.shared.messaging.email

import com.rj.ecommerce.api.shared.enums.NotificationTemplate
import com.rj.ecommerce.api.shared.messaging.contract.MessageVersioning
import java.time.LocalDateTime
import java.util.UUID

/**
 * Holds the metadata common to all email messages.
 */
data class MessageEnvelope(
    val to: String,
    val subject: String,
    val template: NotificationTemplate,
    val correlationId: String,
    val messageId: String = UUID.randomUUID().toString(),
    val version: String = MessageVersioning.CURRENT_VERSION,
    val timestamp: LocalDateTime = LocalDateTime.now()
)