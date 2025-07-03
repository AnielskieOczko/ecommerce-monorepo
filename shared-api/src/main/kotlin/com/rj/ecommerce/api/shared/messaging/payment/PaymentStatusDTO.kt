package com.rj.ecommerce.api.shared.messaging.payment

import com.rj.ecommerce.api.shared.enums.CanonicalPaymentStatus
import java.time.LocalDateTime

data class PaymentStatusDTO(
    val orderId: Long,
    val paymentStatus: CanonicalPaymentStatus,
    val paymentTransactionId: String?,
    val lastUpdate: LocalDateTime?
)