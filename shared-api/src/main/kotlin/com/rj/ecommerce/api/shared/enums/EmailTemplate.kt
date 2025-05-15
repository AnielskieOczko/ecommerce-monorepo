package com.rj.ecommerce.api.shared.enums // Or your equivalent Kotlin package

enum class EmailTemplate(val templateId: String) { // Property defined in primary constructor
    // Order templates
    ORDER_CONFIRMATION("order-confirmation"),
    ORDER_SHIPMENT("order-shipment"),
    ORDER_CANCELLED("order-cancelled"),
    ORDER_REFUNDED("order-refunded"),
    CUSTOMER_WELCOME("customer-welcome"),

    // Payment templates
    PAYMENT_CONFIRMATION("payment-confirmation"),
    PAYMENT_FAILED("payment-failed"),
    PAYMENT_ERROR_ADMIN("payment-error-admin"),
    PAYMENT_ERROR_CUSTOMER("payment-error-customer"),

    // Test template
    TEST_MESSAGE("test-message-template"); // Semicolon optional

    companion object {
        // Cache for efficient lookup by templateId
        private val templateIdToInstanceMap: Map<String, EmailTemplate> by lazy {
            entries.associateBy { it.templateId }
        }

        /**
         * Retrieves an EmailTemplate enum constant by its templateId string.
         * This lookup is case-sensitive.
         *
         * @param templateId The string ID of the template.
         * @return The corresponding EmailTemplate enum.
         * @throws IllegalArgumentException if no template matches the given templateId.
         */
        @JvmStatic // Optional for Java interop
        fun fromTemplateId(templateId: String): EmailTemplate {
            return templateIdToInstanceMap[templateId]
                ?: throw IllegalArgumentException(
                    "No EmailTemplate found for templateId: '$templateId'. " +
                            "Known templateIds are: ${entries.joinToString { "'${it.templateId}'" }}"
                )
        }

        /**
         * Retrieves an EmailTemplate enum constant by its templateId string, ignoring case.
         * Returns null if no template matches or if the input is null/blank.
         *
         * @param templateId The string ID of the template (case-insensitive).
         * @return The corresponding EmailTemplate enum, or null if not found.
         */
        @JvmStatic
        fun fromTemplateIdOrNull(templateId: String?, ignoreCase: Boolean = true): EmailTemplate? {
            if (templateId.isNullOrBlank()) {
                return null
            }
            return if (ignoreCase) {
                entries.find { it.templateId.equals(templateId, ignoreCase = true) }
            } else {
                templateIdToInstanceMap[templateId] // Use the map for case-sensitive and potentially faster lookup
            }
        }
    }
}
