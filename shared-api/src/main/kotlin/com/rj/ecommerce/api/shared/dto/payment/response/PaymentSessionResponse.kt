package com.rj.ecommerce.api.shared.dto.payment.response

import com.rj.ecommerce.api.shared.enums.CanonicalPaymentStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Response to the client containing the details of a newly created payment session.")
data class PaymentSessionResponse(
    @field:Schema(description = "The ID of the order associated with this payment session.")
    val orderId: Long,

    @field:Schema(description = "The unique ID of the payment session from the provider.")
    val sessionId: String?,

    @field:Schema(description = "The URL the client should redirect the user to for completing the payment.")
    val sessionUrl: String?,

    @field:Schema(description = "The timestamp when this payment session expires.")
    val expiresAt: LocalDateTime?,

    @field:Schema(description = "The current status of the payment.")
    val paymentStatus: CanonicalPaymentStatus
)