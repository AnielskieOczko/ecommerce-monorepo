package com.rj.ecommerce.api.shared.enums

/**
 * Represents the delivery status of an email as reported by an external email service.
 * This is used in the `EmailDeliveryReceiptDTO`.
 */
enum class EmailDeliveryReceiptStatus {
    /** The email was successfully delivered to the recipient's mail server. */
    DELIVERED,
    /** The email was permanently rejected (e.g., invalid address). This is a hard bounce. */
    BOUNCED,
    /** The recipient opened the email. */
    OPENED,
    /** The recipient clicked a link in the email. */
    CLICKED,
    /** The email was marked as spam by the recipient. */
    SPAM_COMPLAINT
}