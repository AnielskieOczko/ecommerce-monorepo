package com.rj.ecommerce.api.shared.messaging.email

import com.rj.ecommerce.api.shared.enums.EmailTemplate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Request to send a welcome email.
 *
 * @property messageId Unique ID for this message.
 * @property version Message format version.
 * @property to Email address of the recipient.
 * @property subject Optional subject line (may be generated from template).
 * @property template Email template to use.
 * @property additionalData Additional context-specific data for the template.
 * @property timestamp Time when the message was created.
 * @property customerName Name of the customer to welcome.
 *
 * Requirements:
 * - messageId, version, to, template, timestamp, and customerName are required
 * - subject and additionalData are optional
 */
data class WelcomeEmailRequest(
    val messageId: UUID,
    val version: String,
    val to: String,
    val subject: String? = null,
    val template: EmailTemplate,
    val additionalData: Map<String, Any>? = null,
    val timestamp: LocalDateTime,
    val customerName: String
)
