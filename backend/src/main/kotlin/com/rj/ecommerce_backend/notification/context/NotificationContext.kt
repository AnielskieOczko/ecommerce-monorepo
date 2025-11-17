package com.rj.ecommerce_backend.notification.context

import com.rj.ecommerce_backend.api.shared.enums.OrderStatus
import com.rj.ecommerce_backend.order.domain.Order

/**
 * A sealed interface representing the possible data contexts for a notification.
 * This provides a type-safe way to pass specific data (like an Order)
 * without resorting to unsafe casting from a generic Map.
 */
sealed interface NotificationContext {
    /** A context for notifications that require a full Order object. */
    data class OrderContext(val order: Order) : NotificationContext

    /** Add a context for status updates. */
    data class OrderStatusUpdateContext(val order: Order, val previousStatus: OrderStatus) : NotificationContext

    /** A context for notifications that do not require any specific data. */
    data object EmptyContext : NotificationContext
}