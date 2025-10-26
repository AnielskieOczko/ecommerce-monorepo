package com.rj.ecommerce_backend.api.shared.dto.payment.response

import com.rj.ecommerce_backend.api.shared.enums.PaymentMethod
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Describes a single available payment option for the client to render.")
data class PaymentOptionDetails(
    @field:Schema(
        description = "The programmatic enum key for the payment method. The frontend will send this back when an order is created.",
        required = true,
        example = "CREDIT_CARD"
    )
    val method: PaymentMethod,

    @field:Schema(
        description = "The human-readable name to display in the UI.",
        example = "Credit / Debit Card",
        required = true
    )
    val displayName: String,

    @field:Schema(
        description = "URL to an icon representing the payment method.",
        example = "/icons/credit_card.svg",
        required = false
    )
    val iconUrl: String?
)