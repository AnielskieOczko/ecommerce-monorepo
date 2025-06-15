package com.rj.ecommerce.api.shared.messaging.payment

data class CheckoutUrlsRequestDTO(
    val successUrl: String,
    val cancelUrl: String
)