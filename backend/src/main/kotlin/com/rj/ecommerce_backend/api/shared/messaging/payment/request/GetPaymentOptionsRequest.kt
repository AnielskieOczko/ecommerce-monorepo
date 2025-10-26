package com.rj.ecommerce_backend.api.shared.messaging.payment.request

// The message the Main Backend sends. It can be empty for now.
data class GetPaymentOptionsRequest(val context: Map<String, String> = emptyMap()) // For future use, e.g., currency