package com.rj.ecommerce_backend.api.shared.dto.payment.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Request from the client containing the success and cancellation URLs for the payment session.")
data class PaymentSessionUrlRequest(
    @field:Schema(description = "The full URL to redirect the user to upon successful payment.", required = true)
    @field:NotBlank
    val successUrl: String,

    @field:Schema(description = "The full URL to redirect the user to if they cancel the payment.", required = true)
    @field:NotBlank
    val cancelUrl: String
)