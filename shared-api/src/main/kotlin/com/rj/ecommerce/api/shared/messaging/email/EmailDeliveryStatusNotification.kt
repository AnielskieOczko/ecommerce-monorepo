package com.rj.ecommerce.api.shared.messaging.email

import com.rj.ecommerce.api.shared.enums.EmailStatus
import java.time.LocalDateTime
import java.util.UUID

/**
 * Notification about the delivery status of a sent email.
 *
 * @property messageId ID of this status message.
 * @property version Message format version.
 * @property originalMessageId ID of the email this status refers to.
 * @property status Delivery status of the email.
 * @property recipientEmail Email address of the recipient.
 * @property errorMessage Error message if delivery failed.
 * @property providerStatusDetails Raw status details from the email provider.
 * @property timestamp Time when the status was updated.
 *
 * Requirements:
 * - messageId, version, status, and timestamp are required
 * - originalMessageId, recipientEmail, errorMessage, and providerStatusDetails are optional
 */
data class EmailDeliveryStatusNotification(
    val messageId: UUID,
    val version: String,
    val originalMessageId: UUID? = null,
    val status: EmailStatus,
    val recipientEmail: String? = null,
    val errorMessage: String? = null,
    val providerStatusDetails: Map<String, Any>? = null,
    val timestamp: LocalDateTime
)
