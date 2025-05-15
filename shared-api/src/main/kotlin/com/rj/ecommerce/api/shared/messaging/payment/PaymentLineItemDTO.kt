package com.rj.ecommerce.api.shared.messaging.payment

/**
 * Represents a single item in a payment request.
 *
 * @property name Name of the item.
 * @property description Optional description of the item.
 * @property unitAmountCents Price in smallest currency unit (e.g., cents).
 * @property quantity Number of units.
 * @property currencyCode ISO 4217 Currency Code (e.g., USD).
 *
 * Requirements:
 * - name, unitAmountCents, quantity, and currencyCode are required
 * - description is optional
 * - currencyCode must be a 3-letter ISO 4217 code
 * - unitAmountCents must be a positive integer
 * - quantity must be at least 1
 */
data class PaymentLineItemDTO(
    val name: String,
    val description: String? = null,
    val unitAmountCents: Long,
    val quantity: Int,
    val currencyCode: String
) {
    init {
        require(unitAmountCents > 0) { "Unit amount must be positive" }
        require(quantity >= 1) { "Quantity must be at least 1" }
        require(currencyCode.matches(Regex("^[A-Z]{3}$"))) {
            "Currency code must be a 3-letter ISO 4217 code"
        }
    }
}
