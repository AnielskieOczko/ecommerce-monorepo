package com.rj.ecommerce.api.shared.enums

/**
 * Represents the status of an order.
 *
 * Values:
 * - PENDING: Order has been created but not yet confirmed
 * - CONFIRMED: Order has been confirmed but processing has not started
 * - PROCESSING: Order is being processed
 * - SHIPPED: Order has been shipped
 * - DELIVERED: Order has been delivered to the customer
 * - CANCELLED: Order has been cancelled
 * - REFUNDED: Order has been refunded
 * - FAILED: Order processing has failed
 */
enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED,
    FAILED
}