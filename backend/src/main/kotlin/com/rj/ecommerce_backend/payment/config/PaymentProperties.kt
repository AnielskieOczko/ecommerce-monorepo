package com.rj.ecommerce_backend.payment.config

import com.rj.ecommerce_backend.api.shared.enums.PaymentMethod
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "app.payment")
@Validated
data class PaymentProperties(

    val providers: Map<String, ProviderConfig> = emptyMap()
)

@Validated
data class ProviderConfig(
    val enabled: Boolean = false,
    val displayName: String = "",
    val apiKey: String? = null,
    val webhookSecret: String? = null,
    val clientId: String? = null,
    val clientSecret: String? = null,
    val supportedMethods: List<PaymentMethod> = emptyList()
)