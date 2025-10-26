package com.rj.ecommerce_backend.payment.config

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "app.stripe")
@Validated
data class StripeProperties(
    @field:NotBlank val secretKey: String,
    @field:NotBlank val publishableKey: String,
    @field:NotBlank val webhookSecret: String
)