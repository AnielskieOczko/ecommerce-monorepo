package com.rj.ecommerce_backend.api.shared.messaging.payment.request

import com.rj.ecommerce.api.shared.messaging.payment.common.PaymentLineItem

/**
 * Asynchronous request from the Backend to the Payment Service to create a payment session.
 */
data class PaymentInitiationRequest(
    val orderId: Long,
    val customerEmail: String,
    val successUrl: String,
    val cancelUrl: String,
    val providerIdentifier: String, // e.g., "STRIPE"
    val lineItems: List<PaymentLineItem>,
    val metadata: Map<String, String>? = null
)
