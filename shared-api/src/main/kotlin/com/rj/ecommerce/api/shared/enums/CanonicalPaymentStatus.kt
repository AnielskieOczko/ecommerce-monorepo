package com.rj.ecommerce.api.shared.enums

/**
 * Represents the standardized, provider-agnostic status of a payment
 * within the ecommerce ecosystem.
 */
enum class CanonicalPaymentStatus {
    /** The payment has been initiated but is not yet confirmed. */
    PENDING,
    /** The payment was successfully processed and funds are confirmed. */
    SUCCEEDED,
    /** The payment was declined or failed. */
    FAILED,
    /** The payment session expired before the user could complete it. */
    EXPIRED,
    /** The payment was explicitly canceled by the user. */
    CANCELED,
    /** The status is unknown or could not be determined. */
    UNKNOWN
}