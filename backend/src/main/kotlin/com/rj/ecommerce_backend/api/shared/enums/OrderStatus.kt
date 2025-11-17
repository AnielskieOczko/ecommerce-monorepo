package com.rj.ecommerce_backend.api.shared.enums // Or your equivalent Kotlin package

enum class OrderStatus {
    PENDING,        // Order created but not confirmed
    CONFIRMED,      // Order confirmed (payment successful)
    PROCESSING,     // Order is being prepared for shipment
    SHIPPED,        // Order has been shipped
    DELIVERED,      // Order has been delivered
    CANCELLED,      // Order has been cancelled
    REFUNDED,       // Order has been refunded
    FAILED;         // Order failed (e.g., payment failed)

    companion object {
        /**
         * Converts a string to the corresponding OrderStatus enum.
         * This method is case-sensitive by default (matching Enum.valueOf behavior),
         * but made more robust by converting input to uppercase.
         *
         * @param statusString The string representation of the order status.
         * @return The corresponding OrderStatus enum.
         * @throws IllegalArgumentException if the string does not match any enum constant.
         */
        @JvmStatic // Optional: if you need to call this from Java code as OrderStatus.fromString()
        fun fromString(statusString: String): OrderStatus {
            return try {
                // valueOf is case-sensitive. Convert input to uppercase to match enum constant names.
                valueOf(statusString.uppercase())
            } catch (e: IllegalArgumentException) {
                // Provide a more informative error message
                throw IllegalArgumentException(
                    "Invalid order status string: '$statusString'. " +
                            "Allowed values are: ${entries.joinToString { it.name }}",
                    e
                )
            }
        }

        /**
         * Converts a string to the corresponding OrderStatus enum, ignoring case.
         * Returns null if the string does not match any enum constant or is null/blank.
         * This is often a more user-friendly approach for external inputs.
         *
         * @param statusString The string representation of the order status (case-insensitive).
         * @return The corresponding OrderStatus enum, or null if not found or input is invalid.
         */
        @JvmStatic
        fun fromStringOrNull(statusString: String?): OrderStatus? {
            if (statusString.isNullOrBlank()) {
                return null
            }
            return entries.find { it.name.equals(statusString, ignoreCase = true) }
        }
    }
}