package com.rj.ecommerce_backend.events.order

import com.rj.ecommerce_backend.order.domain.Order
import org.springframework.context.ApplicationEvent

/**
 * An application event that is published when a new order is successfully created.
 */
class OrderCreatedEvent(
    source: Any,
    val order: Order
) : ApplicationEvent(source)