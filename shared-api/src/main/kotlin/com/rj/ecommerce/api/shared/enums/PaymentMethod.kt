package com.rj.ecommerce.api.shared.enums

/**
 * Represents the method used for payment.
 *
 * Values:
 * - CREDIT_CARD: Payment using a credit card
 * - PAYPAL: Payment using PayPal
 * - BANK_TRANSFER: Payment using a bank transfer
 * - BLIK: Payment using BLIK (Polish payment system)
 */
enum class PaymentMethod {
    CREDIT_CARD,
    PAYPAL,
    BANK_TRANSFER,
    BLIK
}
