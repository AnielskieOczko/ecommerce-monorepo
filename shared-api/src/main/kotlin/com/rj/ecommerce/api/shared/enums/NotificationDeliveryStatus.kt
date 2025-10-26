package com.rj.ecommerce.api.shared.enums

/**
 * A canonical, channel-agnostic representation of a notification's delivery status.
 * Specific provider statuses (e.g., SendGrid's "processed", Twilio's "sent")
 * should be mapped to one of these standard statuses.
 */
enum class NotificationDeliveryStatus {
    /** The notification was successfully delivered to the end user's device or mailbox. */
    DELIVERED,

    /** The notification failed to be delivered and will not be retried. (e.g., invalid address/number, hard bounce). */
    FAILED,

    /** The status is unknown or could not be determined from the provider's response. */
    UNKNOWN
}