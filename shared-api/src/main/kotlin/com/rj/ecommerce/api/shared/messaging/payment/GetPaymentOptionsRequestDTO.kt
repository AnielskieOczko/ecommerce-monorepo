package com.rj.ecommerce.api.shared.messaging.payment

// The message the Main Backend sends. It can be empty for now.
data class GetPaymentOptionsRequestDTO(val context: Map<String, String> = emptyMap()) // For future use, e.g., currency