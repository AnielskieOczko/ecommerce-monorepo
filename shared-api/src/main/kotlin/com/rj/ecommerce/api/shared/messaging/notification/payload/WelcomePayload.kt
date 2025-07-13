package com.rj.ecommerce.api.shared.messaging.notification.payload

data class WelcomePayload(
    val customerName: String,
    val couponCode: String? = null,
    val additionalData: Map<String, Any> = emptyMap()
)