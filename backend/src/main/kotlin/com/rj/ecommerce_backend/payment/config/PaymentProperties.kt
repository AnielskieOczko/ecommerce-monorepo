package com.rj.ecommerce_backend.payment.config

import com.rj.ecommerce_backend.api.shared.enums.PaymentMethod
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "payment")
data class PaymentProperties(
    // The key of the map will be the provider name (e.g., "stripe", "paypal")
    val providers: Map<String, ProviderConfig> = emptyMap()
)

data class ProviderConfig(
    val enabled: Boolean = false,
    val displayName: String = "",
    val apiKey: String? = null,
    val webhookSecret: String? = null,
    val clientId: String? = null,
    val clientSecret: String? = null,
    val supportedMethods: List<PaymentMethod> = emptyList()
)