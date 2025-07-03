package com.rj.ecommerce.api.shared.messaging.payment

import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.enums.CanonicalPaymentStatus
import java.time.LocalDateTime

/**
 * Asynchronous notification (webhook) from Payment Service to Backend about payment events.
 *
 * @property eventId Unique ID for this event from the provider.
 * @property eventType Type of event (e.g., payment_intent.succeeded).
 * @property orderId Your internal order ID (often retrieved from metadata).
 * @property paymentId Payment provider's transaction/intent ID.
 * @property status Status of the payment.
 * @property amount Amount of the payment.
 * @property paidAt Time when the payment was made.
 * @property rawProviderPayload Full event payload from provider for auditing/debugging.
 *
 * Requirements:
 * - eventId, eventType, orderId, paymentId, and status are required
 * - amount, paidAt, and rawProviderPayload are optional
 */
data class PaymentNotificationDTO(
    val eventId: String, // Unique ID for the event from the payment provider
    val eventType: String, // e.g., "payment.succeeded", "charge.failed"
    val orderId: Long, // Your system's order ID
    val paymentId: String, // Payment provider's ID for the payment transaction
    val status: CanonicalPaymentStatus, // Your system's standardized status
    val amount: Money? = null, // Amount associated with this specific event (e.g., capture amount, refund amount)
    val paidAt: LocalDateTime? = null, // Timestamp when the payment was successfully processed or specific event occurred
    val rawProviderPayload: Map<String, Any>? = null // Raw data from the payment provider (webhook payload)
)
