package com.rj.ecommerce.api.shared.messaging.email

/**
 * A generic, top-level container for any email request.
 * @param T The type of the specific payload for this email.
 */
data class EmailRequest<T>(
    val envelope: MessageEnvelope,
    val payload: T
)