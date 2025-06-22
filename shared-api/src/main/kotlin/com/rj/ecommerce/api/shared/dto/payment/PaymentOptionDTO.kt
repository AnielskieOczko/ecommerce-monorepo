package com.rj.ecommerce.api.shared.dto.payment

import com.rj.ecommerce.api.shared.enums.PaymentMethod

data class PaymentOptionDTO(
    val method: PaymentMethod,
    val displayName: String,
    val uiComponentKey: String,
    val clientConfiguration: Map<String, Any>
)