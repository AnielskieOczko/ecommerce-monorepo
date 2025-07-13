package com.rj.ecommerce.api.shared.dto.payment.response

import com.rj.ecommerce.api.shared.enums.PaymentMethod
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Describes a single available payment option for the client to render.")
data class PaymentOptionDetails(
    @field:Schema(description = "The programmatic enum key for the payment method.", required = true)
    val method: PaymentMethod,

    @field:Schema(description = "The human-readable name to display in the UI.", example = "Credit Card", required = true)
    val displayName: String,

    @field:Schema(description = "A key for the frontend to identify which UI component to render.", example = "STRIPE_CARD_ELEMENT", required = true)
    val uiComponentKey: String,

    @field:Schema(description = "A map of client-side configuration needed to initialize the UI component, such as public keys.", required = true)
    val clientConfiguration: Map<String, Any>
)