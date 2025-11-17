package com.rj.ecommerce_backend.api.shared.enums

/**
 * Represents the status of the internal system's attempt to dispatch a notification.
 * This is used for the `EmailNotification` entity in the database.
 */
enum class NotificationDispatchStatus {
    /** The notification has been created but not yet sent to the message queue. */
    PENDING,
    /** The notification was successfully sent to the message queue. */
    SENT,
    /** The attempt to send the notification to the message queue failed. */
    FAILED,

    DELIVERED
}