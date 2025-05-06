package com.rj.ecommerce.api.shared.enums

/**
 * Represents the status of a payment or payment session.
 *
 * Values:
 * - PENDING: Payment is awaiting processing
 * - PAID: Payment has been successfully processed
 * - UNPAID: Payment has not been made
 * - FAILED: Payment processing has failed
 * - REFUNDED: Payment has been refunded
 * - OPEN: Payment session is open and awaiting customer action
 * - COMPLETE: Payment session has been completed
 * - EXPIRED: Payment session has expired
 * - NO_PAYMENT_REQUIRED: No payment is required for this transaction
 */
enum class PaymentStatus {
    PENDING,
    PAID,
    UNPAID,
    FAILED,
    REFUNDED,
    OPEN,
    COMPLETE,
    EXPIRED,
    NO_PAYMENT_REQUIRED
}
