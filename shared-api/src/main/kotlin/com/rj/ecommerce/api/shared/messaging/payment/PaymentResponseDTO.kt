package com.rj.ecommerce.api.shared.messaging.payment

import com.rj.ecommerce.api.shared.enums.PaymentStatus
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
 * - expiresAt and metadata are optional
 */
data class PaymentResponseDTO(
    // Original fields
    val sessionId: String,
    val orderId: Long,
    val sessionStatus: PaymentStatus,
    val paymentStatus: PaymentStatus,
    val checkoutUrl: String,
    val expiresAt: LocalDateTime? = null,
    val metadata: Map<String, String>? = null, // This will replace 'additionalDetails'

    // --- Extended fields required by the notification service ---
    val customerEmail: String,
    val amountTotal: Long?, // Total amount in the smallest currency unit (e.g., cents), nullable for safety
    val currency: String?   // 3-letter ISO currency code, nullable for safety
)
