package com.rj.ecommerce.api.shared.messaging.payment

import com.rj.ecommerce.api.shared.enums.CanonicalPaymentStatus
import java.time.LocalDateTime

/**
 * Response from Payment Service to Backend after initiating payment.
 *
 * @property sessionId Payment provider's session ID.
 * @property orderId ID of the order being paid for.
 * @property sessionStatus Status of the checkout session itself.
 * @property paymentStatus Status of the underlying payment intent.
 * @property checkoutUrl URL for user to complete payment.
 * @property expiresAt Time when the payment session expires.
 * @property metadata Additional data passed through the payment provider.
 *
 * Requirements:
 * - sessionId, orderId, sessionStatus, paymentStatus, and checkoutUrl are required
 * - expiresAt, and metadata is optional
 */
data class PaymentResponseDTO(
    val sessionId: String,
    val orderId: Long,
    val sessionStatus: CanonicalPaymentStatus,
    val paymentStatus: CanonicalPaymentStatus,
    val checkoutUrl: String,
    val expiresAt: LocalDateTime? = null,

    // This field is mandatory for tracing and reliability.
    val correlationId: String,

    val metadata: Map<String, String>? = null,
    val customerEmail: String,
    val amountTotal: Long?,
    val currency: String?
)
