package com.rj.ecommerce_backend.api.shared.dto.payment.response

import com.rj.ecommerce.api.shared.enums.CanonicalPaymentStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Provides the current payment status of an order to the client.")
data class PaymentStatusResponse(
    @field:Schema(description = "The ID of the order.")
    val orderId: Long,

    @field:Schema(description = "The canonical status of the payment.")
    val paymentStatus: CanonicalPaymentStatus,

    @field:Schema(description = "The unique transaction ID from the payment provider, if available.")
    val paymentTransactionId: String?,

    @field:Schema(description = "The timestamp of the last update to the payment status.")
    val lastUpdate: LocalDateTime?
)