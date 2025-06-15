package com.rj.ecommerce.api.shared.messaging.payment

import com.rj.ecommerce.api.shared.enums.PaymentStatus
import java.time.LocalDateTime

data class PaymentStatusDTO(
    val orderId: Long,
    val paymentStatus: PaymentStatus,
    val paymentTransactionId: String?,
    val lastUpdate: LocalDateTime?
)