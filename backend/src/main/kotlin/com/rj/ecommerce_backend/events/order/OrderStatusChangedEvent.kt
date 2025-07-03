package com.rj.ecommerce_backend.events.order

import com.rj.ecommerce.api.shared.enums.OrderStatus
import com.rj.ecommerce_backend.order.domain.Order
import org.springframework.context.ApplicationEvent

class OrderStatusChangedEvent(
    source: Any,
    val order: Order,
    val newStatus: OrderStatus,
    val previousStatus: OrderStatus
) : ApplicationEvent(source)