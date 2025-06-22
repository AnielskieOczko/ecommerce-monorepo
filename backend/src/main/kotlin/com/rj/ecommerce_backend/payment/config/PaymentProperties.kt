package com.rj.ecommerce_backend.payment.config

import com.rj.ecommerce.api.shared.enums.PaymentMethod
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.payment")
data class PaymentProperties(
    var methodToGatewayMapping: Map<PaymentMethod, String> = emptyMap()
)