package com.rj.ecommerce.api.shared.core

import java.math.BigDecimal

/**
 * Represents a monetary value with currency.
 *
 * @property amount Monetary amount (e.g., 19.99). Represents BigDecimal.
 * @property currencyCode ISO 4217 Currency Code (e.g., USD).
 *
 * Requirements:
 * - currencyCode must be a 3-letter ISO 4217 code (e.g., USD, EUR, GBP)
 */
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