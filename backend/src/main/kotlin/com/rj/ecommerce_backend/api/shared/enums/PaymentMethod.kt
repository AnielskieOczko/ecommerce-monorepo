package com.rj.ecommerce_backend.api.shared.enums

enum class PaymentMethod {
    CREDIT_CARD,
    BANK_TRANSFER,
    BLIK;

    companion object {
        /**
         * Converts a string to the corresponding PaymentMethod enum.
         * This method is case-sensitive, matching Enum.valueOf behavior.
         *
         * @param method The string representation of the payment method.
         * @return The corresponding PaymentMethod enum.
         * @throws IllegalArgumentException if the string does not match any enum constant.
         */
        @JvmStatic
        fun fromString(method: String): PaymentMethod {
            return try {
                // valueOf is case-sensitive in Kotlin (and Java) for enums
                valueOf(method.uppercase()) // Standardizing to uppercase for robustness, matching enum constant names
            } catch (e: IllegalArgumentException) {
                // Provide a more informative error message
                throw IllegalArgumentException(
                    "Invalid payment method string: '$method'. " +
                            "Allowed values are: ${entries.joinToString { it.name }}",
                    e
                )
            }
        }

        /**
         * Converts a string to the corresponding PaymentMethod enum, ignoring case.
         * Returns null if the string does not match any enum constant.
         * This is often a more user-friendly approach for external inputs.
         *
         * @param method The string representation of the payment method (case-insensitive).
         * @return The corresponding PaymentMethod enum, or null if not found.
         */
        @JvmStatic
        fun fromStringOrNull(method: String?): PaymentMethod? {
            if (method.isNullOrBlank()) {
                return null
            }
            return entries.find { it.name.equals(method, ignoreCase = true) }
        }
    }
}