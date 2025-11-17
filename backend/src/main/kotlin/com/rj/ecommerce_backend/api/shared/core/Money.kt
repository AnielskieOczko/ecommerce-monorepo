package com.rj.ecommerce_backend.api.shared.core

import com.rj.ecommerce_backend.api.shared.enums.Currency
import jakarta.persistence.Embeddable
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
@Embeddable
data class Money(
    val amount: BigDecimal,
    val currencyCode: Currency
) {
    init {
        require(currencyCode.name.matches(Regex("^[A-Z]{3}$"))) {
            "Currency code must be a 3-letter ISO 4217 code"
        }
    }
}