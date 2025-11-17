package com.rj.ecommerce_backend.api.shared.enums // Or your equivalent Kotlin package

/**
 * Defines all available notification templates in the system, independent of the delivery channel.
 *
 * @param templateId The unique identifier used to locate the template file (e.g., 'order-confirmation').
 * @param description A human-readable description of the template's purpose.
 */
enum class NotificationTemplate(
    val templateId: String,
    val description: String
) {
    // --- Order-Related Templates ---
    ORDER_CONFIRMATION("order-confirmation", "Sent to a customer after an order is successfully created and paid for."),
    ORDER_SHIPPED("order-shipment", "Sent when an order's status changes to SHIPPED."),
    ORDER_DELIVERED("order-delivered", "Sent when an order's status changes to DELIVERED."),
    ORDER_CANCELLED("order-cancelled", "Sent when an order is cancelled by the user or an admin."),
    ORDER_REFUNDED("order-refunded", "Sent when a refund has been processed for an order."),

    // --- Customer-Related Templates ---
    CUSTOMER_WELCOME("customer-welcome", "Sent to a new user upon successful registration."),

    // --- Payment-Related Templates ---
    PAYMENT_CONFIRMATION("payment-confirmation", "Sent to a customer after a payment is successfully processed."),
    PAYMENT_FAILED("payment-failed", "Sent to a customer when their payment attempt fails."),

    // --- Administrative Templates ---
    PAYMENT_ERROR_ADMIN("payment-error-admin", "Alert sent to administrators when a payment processing error occurs."),
    PAYMENT_ERROR_CUSTOMER(
        "payment-error-customer",
        "Sent to a customer when a system error occurs during payment processing."
    ),

    // --- Test Template ---
    TEST_MESSAGE("test-message-template", "A generic template for testing the notification system.");

    companion object {
        // A map for efficient, case-insensitive lookup.
        private val templateIdToInstanceMap: Map<String, NotificationTemplate> by lazy {
            entries.associateBy { it.templateId.lowercase() }
        }

        /**
         * Retrieves a NotificationTemplate enum constant by its templateId string.
         * This lookup is case-insensitive.
         *
         * @param templateId The string ID of the template (e.g., "order-confirmation").
         * @return The corresponding NotificationTemplate enum.
         * @throws IllegalArgumentException if no template matches the given templateId.
         */
        @JvmStatic
        fun fromTemplateId(templateId: String?): NotificationTemplate {
            when {
                templateId.isNullOrBlank() -> {
                    throw IllegalArgumentException("Template ID cannot be null or blank.")
                }
                else -> return templateIdToInstanceMap[templateId.lowercase()]
                    ?: throw IllegalArgumentException(
                        "No NotificationTemplate found for templateId: '$templateId'. " +
                                "Known templateIds are: ${entries.joinToString { "'${it.templateId}'" }}"
                    )
            }
        }
    }
}
