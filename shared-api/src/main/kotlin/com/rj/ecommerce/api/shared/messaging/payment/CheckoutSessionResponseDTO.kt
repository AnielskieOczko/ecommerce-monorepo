package com.rj.ecommerce.api.shared.messaging.payment

import com.rj.ecommerce.api.shared.enums.PaymentStatus

data class CheckoutSessionResponseDTO(
    val orderId: Long,
    val customerEmail: String,
    val sessionId: String,
    val checkoutUrl: String,
    val paymentStatus: PaymentStatus,
    val amountTotal: Long?,
    val currency: String?,
    val additionalDetails: Map<String, String>? = null
)