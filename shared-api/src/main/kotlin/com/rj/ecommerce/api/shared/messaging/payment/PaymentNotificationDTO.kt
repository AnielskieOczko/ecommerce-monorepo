package com.rj.ecommerce.api.shared.messaging.payment

import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.enums.PaymentStatus
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
data class PaymentNotification(
    val eventId: String,
    val eventType: String,
    val orderId: String,
    val paymentId: String,
    val status: PaymentStatus,
    val amount: Money? = null,
    val paidAt: LocalDateTime? = null,
    val rawProviderPayload: Map<String, Any>? = null
)
