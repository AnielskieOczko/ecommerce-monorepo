package com.rj.ecommerce.api.shared.core

import java.math.BigDecimal

data class Money(
    val amount: BigDecimal,
    val currencyCode: String
) {
    init {
        require(currencyCode.matches(Regex("^[A-Z]{3}$"))) {
            "Currency code must be a 3-letter ISO 4217 code"
        }
    }
}