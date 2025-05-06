package com.rj.ecommerce.api.shared.enums

/**
 * Delivery status of an email message.
 *
 * Values:
 * - PENDING: Email is queued for delivery
 * - SENT: Email has been sent
 * - DELIVERED: Email has been delivered to the recipient's server
 * - FAILED: Email delivery has failed
 * - BOUNCED: Email was rejected by the recipient's server
 */
enum class EmailStatus {
    PENDING,
    SENT,
    DELIVERED,
    FAILED,
    BOUNCED
}
