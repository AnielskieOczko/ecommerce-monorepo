package com.rj.ecommerce_backend.events

/**
 * Describes who or what initiated an action, like a cancellation.
 */
enum class CancellationActor {
    USER,
    ADMIN,
    SYSTEM // e.g., for automatic cancellation due to payment timeout
}