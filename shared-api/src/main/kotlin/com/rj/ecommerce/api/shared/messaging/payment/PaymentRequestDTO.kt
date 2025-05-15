package com.rj.ecommerce.api.shared.messaging.payment

/**
 * Request from Backend to Payment Service to initiate payment.
 *
 * @property orderId ID of the order being paid for.
 * @property customerEmail Email of the customer making the payment.
 * @property successUrl URL to redirect to on successful payment.
 * @property cancelUrl URL to redirect to if payment is cancelled.
 * @property lineItems List of items being paid for.
 * @property metadata Additional data to pass through the payment provider.
 *
 * Requirements:
 * - orderId, customerEmail, successUrl, cancelUrl, and lineItems are required
 * - metadata is optional
 * - successUrl and cancelUrl must be valid URLs
 */
data class PaymentRequestDTO(
    val orderId: String,
    val customerEmail: String,
    val successUrl: String,
    val cancelUrl: String,
    val lineItems: List<PaymentLineItemDTO>,
    val metadata: Map<String, String>? = null
)
