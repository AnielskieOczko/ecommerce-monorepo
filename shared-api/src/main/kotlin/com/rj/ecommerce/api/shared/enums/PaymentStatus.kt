package com.rj.ecommerce.api.shared.enums // Or your equivalent Kotlin package

/**
 * Enum representing Stripe payment statuses.
 * Based on Stripe's official payment and checkout session statuses.
 * @see <a href="https://stripe.com/docs/api/charges/object#charge_object-status">Stripe Charge Status</a>
 * @see <a href="https://stripe.com/docs/api/checkout/sessions/object#checkout_session_object-payment_status">Stripe Checkout Session Payment Status</a>
 * @see <a href="https://stripe.com/docs/api/checkout/sessions/object#checkout_session_object-status">Stripe Checkout Session Status</a>
 */
@Deprecated("Use enum CanonicalPaymentStatus")
enum class PaymentStatus(val stripeStatus: String) { // Primary constructor with 'val' property
    // Charge statuses
    SUCCEEDED("succeeded"),
    PENDING("pending"),
    FAILED("failed"),

    // Checkout Session payment_status
    PAID("paid"),
    UNPAID("unpaid"),
    NO_PAYMENT_REQUIRED("no_payment_required"),

    // Checkout Session status
    OPEN("open"),
    COMPLETE("complete"),
    EXPIRED("expired"),

    // Special case
    UNKNOWN("unknown"); // Semicolon is optional if companion object is the last member

    companion object { // For static-like factory methods
        /**
         * Converts a Stripe charge status to the corresponding PaymentStatus enum.
         * Returns UNKNOWN if the status is null, empty, or not recognized.
         */
        @JvmStatic // Optional: if you need to call this from Java code as a static method
        fun fromChargeStatus(stripeStatus: String?): PaymentStatus {
            return when (stripeStatus?.lowercase()) { // Safe call and Elvis for null/empty
                "succeeded" -> SUCCEEDED
                "pending" -> PENDING
                "failed" -> FAILED
                else -> UNKNOWN
            }
        }

        /**
         * Converts a Stripe checkout session payment status to the corresponding PaymentStatus enum.
         * Returns UNKNOWN if the status is null, empty, or not recognized.
         */
        @JvmStatic
        fun fromCheckoutSessionPaymentStatus(stripeStatus: String?): PaymentStatus {
            return when (stripeStatus?.lowercase()) {
                "paid" -> PAID
                "unpaid" -> UNPAID
                "no_payment_required" -> NO_PAYMENT_REQUIRED
                else -> UNKNOWN
            }
        }

        /**
         * Converts a Stripe checkout session status to the corresponding PaymentStatus enum.
         * Returns UNKNOWN if the status is null, empty, or not recognized.
         */
        @JvmStatic
        fun fromCheckoutSessionStatus(stripeStatus: String?): PaymentStatus {
            return when (stripeStatus?.lowercase()) {
                "open" -> OPEN
                "complete" -> COMPLETE
                "expired" -> EXPIRED
                else -> UNKNOWN
            }
        }

        // Alternative: A more generic finder if you have many such methods
        // or want to avoid duplicating the when/else logic.
        // You could also create a map for faster lookups if performance is critical for many statuses.
        private val stringToStatusMap: Map<String, PaymentStatus> by lazy {
            entries.associateBy { it.stripeStatus.lowercase() }
        }

        @JvmStatic
        fun fromStripeStatusString(stripeStatus: String?): PaymentStatus {
            if (stripeStatus.isNullOrBlank()) return UNKNOWN
            return stringToStatusMap[stripeStatus.lowercase()] ?: UNKNOWN
        }
    }
}