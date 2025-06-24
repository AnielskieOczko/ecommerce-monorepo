package com.rj.ecommerce.api.shared.messaging.payment

import com.rj.ecommerce.api.shared.enums.CanonicalPaymentStatus
import com.rj.ecommerce.api.shared.enums.PaymentStatus
import java.time.LocalDateTime

/**
 * Defines the data sent from the Payment Service back to a consumer
 * after a payment initiation attempt. This is the canonical contract.
 *
 * @param orderId The unique identifier for the order in the core system.
 * @param customerEmail The email of the customer, for notification purposes.
 * @param sessionId The unique session/transaction ID from the external payment provider.
 * @param checkoutUrl The URL the user must be redirected to to complete the payment.
 * @param paymentStatus The status of the underlying payment intent (e.g., PAID, UNPAID).
 * @param sessionStatus The status of the checkout session itself (e.g., OPEN, COMPLETE, EXPIRED).
 * @param correlationId The unique ID that links this response back to the original request.
 * @param amountTotal The total amount of the transaction in the smallest currency unit (e.g., cents). Nullable for error cases.
 * @param currency The 3-letter ISO currency code. Nullable for error cases.
 * @param expiresAt The timestamp when the checkout session will expire. Nullable.
 * @param metadata A map of any additional data from the provider. Nullable.
 */
data class CheckoutSessionResponseDTO(
    val orderId: Long,
    val customerEmail: String,
    val sessionId: String,
    val checkoutUrl: String,
    val paymentStatus: CanonicalPaymentStatus,
    val sessionStatus: CanonicalPaymentStatus,
    val correlationId: String,
    val amountTotal: Long?,
    val currency: String?,
    val expiresAt: LocalDateTime? = null,
    val metadata: Map<String, String>? = null
)