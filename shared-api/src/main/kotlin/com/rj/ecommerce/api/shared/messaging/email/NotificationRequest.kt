package com.rj.ecommerce.api.shared.messaging.email

/**
 * A generic, top-level container for any email request.
 * @param T The type of the specific payload for this email.
 */
data class NotificationRequest<T>(
    val envelope: NotificationEnvelope,
    val payload: T
)