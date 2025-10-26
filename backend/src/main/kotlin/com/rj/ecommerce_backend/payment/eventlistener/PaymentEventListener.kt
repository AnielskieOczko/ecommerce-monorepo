package com.rj.ecommerce_backend.payment.eventlistener

import com.rj.ecommerce_backend.events.payment.PaymentFailedEvent
import com.rj.ecommerce_backend.events.payment.PaymentSucceededEvent
import com.rj.ecommerce_backend.notification.dispatcher.PaymentNotificationDispatcher
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class PaymentEventListener(
    private val paymentNotificationDispatcher: PaymentNotificationDispatcher
) {
    @TransactionalEventListener
    fun onPaymentSucceeded(event: PaymentSucceededEvent) {
        paymentNotificationDispatcher.sendPaymentSuccess(event.order)
    }

    @TransactionalEventListener
    fun onPaymentFailed(event: PaymentFailedEvent) {
        paymentNotificationDispatcher.sendPaymentFailed(event.order)
    }
}