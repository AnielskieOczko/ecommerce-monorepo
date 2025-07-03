package com.rj.ecommerce_backend.events.order

import com.rj.ecommerce_backend.events.CancellationActor
import com.rj.ecommerce_backend.order.domain.Order
import org.springframework.context.ApplicationEvent

/**
 * An application event published when an order is successfully cancelled.
 * It contains the order and the actor who performed the cancellation.
 */
class OrderCancelledEvent(
    source: Any,
    val order: Order,
    val actor: CancellationActor
) : ApplicationEvent(source)