package com.rj.ecommerce.api.shared.messaging.payment.response

import com.rj.ecommerce.api.shared.enums.CanonicalPaymentStatus
import java.time.LocalDateTime

/**
 * Asynchronous response from the Payment Service to the Backend after a payment initiation attempt.
 * This is the single, canonical contract for this interaction.
 */
data class PaymentInitiationResponse(
    val orderId: Long,
    val sessionId: String,
    val checkoutUrl: String?, // Nullable in case of an immediate failure to generate a URL
    val paymentStatus: CanonicalPaymentStatus,
    val sessionStatus: CanonicalPaymentStatus,
    val correlationId: String,
    val customerEmail: String,
    val amountTotal: Long?,
    val currency: String?,
    val expiresAt: LocalDateTime? = null,
    val metadata: Map<String, String>? = null,
    val errorMessage: String? = null // Field to carry error details in case of failure
)
