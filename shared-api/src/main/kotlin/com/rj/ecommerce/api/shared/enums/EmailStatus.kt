package com.rj.ecommerce.api.shared.enums // Or your equivalent Kotlin package

enum class EmailStatus {
    SENT,
    FAILED,
    DELIVERED,
    OPENED; // Semicolon is optional

    companion object {
        /**
         * Converts a string to the corresponding EmailStatus enum.
         * This method is case-sensitive by default (matching Enum.valueOf behavior),
         * but made more robust by converting input to uppercase.
         *
         * @param statusString The string representation of the email status.
         * @return The corresponding EmailStatus enum.
         * @throws IllegalArgumentException if the string does not match any enum constant.
         */
        @JvmStatic // Optional: if you need to call this from Java code as EmailStatus.fromString()
        fun fromString(statusString: String): EmailStatus {
            return try {
                // valueOf is case-sensitive. Convert input to uppercase to match enum constant names.
                valueOf(statusString.uppercase())
            } catch (e: IllegalArgumentException) {
                // Provide a more informative error message
                throw IllegalArgumentException(
                    "Invalid email status string: '$statusString'. " +
                            "Allowed values are: ${entries.joinToString { it.name }}",
                    e
                )
            }
        }

        /**
         * Converts a string to the corresponding EmailStatus enum, ignoring case.
         * Returns null if the string does not match any enum constant or is null/blank.
         * This is often a more user-friendly approach for external inputs.
         *
         * @param statusString The string representation of the email status (case-insensitive).
         * @return The corresponding EmailStatus enum, or null if not found or input is invalid.
         */
        @JvmStatic
        fun fromStringOrNull(statusString: String?): EmailStatus? {
            if (statusString.isNullOrBlank()) {
                return null
            }
            return entries.find { it.name.equals(statusString, ignoreCase = true) }
        }
    }
}