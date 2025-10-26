package com.rj.ecommerce_backend.api.shared.messaging.payment.notification

import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.enums.CanonicalPaymentStatus
import java.time.LocalDateTime

/**
 * Asynchronous notification from the Payment Service to the Backend, triggered by a provider webhook.
 */
data class PaymentWebhookNotification(
    val eventId: String,
    val eventType: String, // e.g., "checkout.session.completed"
    val orderId: Long,
    val paymentId: String,
    val status: CanonicalPaymentStatus,
    val amount: Money? = null,
    val paidAt: LocalDateTime? = null,
    val rawProviderPayload: Map<String, Any>? = null
)