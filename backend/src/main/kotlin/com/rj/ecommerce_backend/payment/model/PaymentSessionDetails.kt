package com.rj.ecommerce_backend.payment.model

import java.time.LocalDateTime

data class PaymentSessionDetails(
    val sessionId: String,
    val sessionUrl: String,
    val expiresAt: LocalDateTime
)