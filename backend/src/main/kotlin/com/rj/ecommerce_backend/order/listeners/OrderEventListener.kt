package com.rj.ecommerce_backend.order.listeners

import com.rj.ecommerce_backend.notification.dispatcher.OrderNotificationDispatcher
import com.rj.ecommerce_backend.order.domain.events.OrderCancelledEvent
import com.rj.ecommerce_backend.order.domain.events.OrderCreatedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener


@Component
class OrderEventListener(
    private val orderNotificationDispatcher: OrderNotificationDispatcher
) {

    @EventListener
    fun onOrderCreated(event: OrderCreatedEvent) {
        orderNotificationDispatcher.sendOrderConfirmation(event.order)
    }

    /**
     * NEW: This method listens for the OrderCancelledEvent.
     * By using @TransactionalEventListener, this code will only run *after* the
     * transaction that published the event has successfully committed. This prevents
     * sending a cancellation email for an order whose cancellation failed and was rolled back.
     */
    @TransactionalEventListener
    fun onOrderCancelled(event: OrderCancelledEvent) {
        // For now, we send the same notification regardless of the actor.
        // In the future, we could use event.actor to send a different template
        // for admin-initiated cancellations.
        orderNotificationDispatcher.sendOrderCancelled(event.order)
    }
}