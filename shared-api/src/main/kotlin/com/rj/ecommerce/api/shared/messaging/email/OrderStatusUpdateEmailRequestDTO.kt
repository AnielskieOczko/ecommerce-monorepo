package com.rj.ecommerce.api.shared.messaging.email

import com.rj.ecommerce.api.shared.enums.EmailTemplate
import com.rj.ecommerce.api.shared.enums.OrderStatus
import java.time.LocalDateTime
import java.util.UUID

/**
 * Request to send an order status update email.
 *
 * @property messageId Unique ID for this message.
 * @property version Message format version.
 * @property to Email address of the recipient.
 * @property subject Optional subject line (may be generated from template).
 * @property template Email template to use.
 * @property additionalData Additional context-specific data for the template.
 * @property timestamp Time when the message was created.
 * @property orderId ID of the order being updated.
 * @property orderNumber Human-readable order number.
 * @property previousStatus Previous status of the order.
 * @property newStatus New status of the order.
 * @property reason Optional reason for the status change.
 *
 * Requirements:
 * - messageId, version, to, template, timestamp, orderId, and newStatus are required
 * - subject, additionalData, orderNumber, previousStatus, and reason are optional
 */
data class OrderStatusUpdateEmailRequestDTO(
    val messageId: UUID,
    val version: String,
    val to: String,
    val subject: String? = null,
    val template: EmailTemplate,
    val additionalData: Map<String, Any>? = null,
    val timestamp: LocalDateTime,
    val orderId: String,
    val orderNumber: String? = null,
    val previousStatus: OrderStatus? = null,
    val newStatus: OrderStatus,
    val reason: String? = null
)
