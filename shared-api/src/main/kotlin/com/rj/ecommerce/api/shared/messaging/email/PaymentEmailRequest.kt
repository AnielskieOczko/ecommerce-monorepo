package com.rj.ecommerce.api.shared.messaging.email

import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.enums.EmailTemplate
import com.rj.ecommerce.api.shared.enums.PaymentStatus
import java.time.LocalDateTime
import java.util.UUID

/**
 * Request to send a payment status email.
 *
 * @property messageId Unique ID for this message.
 * @property version Message format version.
 * @property to Email address of the recipient.
 * @property subject Optional subject line (may be generated from template).
 * @property template Email template to use.
 * @property additionalData Additional context-specific data for the template.
 * @property timestamp Time when the message was created.
 * @property orderId ID of the order this payment relates to.
 * @property paymentId ID of the payment transaction.
 * @property paymentStatus Status of the payment.
 * @property paymentAmount Amount of the payment.
 *
 * Requirements:
 * - messageId, version, to, template, timestamp, orderId, and paymentStatus are required
 * - subject, additionalData, paymentId, and paymentAmount are optional
 */
data class PaymentEmailRequest(
    val messageId: UUID,
    val version: String,
    val to: String,
    val subject: String? = null,
    val template: EmailTemplate,
    val additionalData: Map<String, Any>? = null,
    val timestamp: LocalDateTime,
    val orderId: String,
    val paymentId: String? = null,
    val paymentStatus: PaymentStatus,
    val paymentAmount: Money? = null
)
