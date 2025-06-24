package com.rj.payment_service.listener

import com.rj.ecommerce.api.shared.messaging.payment.PaymentRequestDTO
import com.rj.payment_service.service.PaymentRequestDispatcher
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class CheckOutSessionListener(
    private val dispatcher: PaymentRequestDispatcher,
) {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    @RabbitListener(queues = ["\${app.rabbitmq.checkout-session-queue}"])
    fun handleCheckoutSessionRequest(request: PaymentRequestDTO, message: Message) {
        val correlationId = message.messageProperties.correlationId
        logger.info { "Received payment request for order ${request.orderId}, dispatching..." }
        // The listener's ONLY job is to delegate.
        dispatcher.dispatch(request, correlationId)
    }

}