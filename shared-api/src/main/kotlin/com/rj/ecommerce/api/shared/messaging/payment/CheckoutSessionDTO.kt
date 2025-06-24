package com.rj.ecommerce.api.shared.messaging.payment

import com.rj.ecommerce.api.shared.enums.CanonicalPaymentStatus
import com.rj.ecommerce.api.shared.enums.PaymentStatus
import java.time.LocalDateTime

data class CheckoutSessionDTO(
    val orderId: Long,
    val sessionId: String?,
    val sessionUrl: String?,
    val expiresAt: LocalDateTime?,
    val paymentStatus: CanonicalPaymentStatus
 )