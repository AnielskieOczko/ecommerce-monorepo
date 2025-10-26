package com.rj.ecommerce_backend.api.shared.messaging.notification.payload

import com.rj.ecommerce.api.shared.core.Money

data class PaymentPayload(
    val orderId: String,
    val paymentId: String?,
    val paymentStatus: String, // e.g., "SUCCEEDED", "FAILED"
    val paymentAmount: Money?,
    val additionalData: Map<String, Any> = emptyMap()
)