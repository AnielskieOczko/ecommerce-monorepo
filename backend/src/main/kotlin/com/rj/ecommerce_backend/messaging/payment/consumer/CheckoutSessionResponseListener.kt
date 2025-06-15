package com.rj.ecommerce_backend.messaging.payment.consumer

import com.rj.ecommerce.api.shared.messaging.payment.PaymentResponseDTO
import com.rj.ecommerce_backend.payment.PaymentWebhookService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

/**
 * A RabbitMQ listener that consumes the response from a checkout session creation process.
 * This is a critical component for updating order status after a payment flow is initiated.
 */
@Component
class CheckoutSessionResponseListener(
    private val paymentWebhookService: PaymentWebhookService
) {

    companion object {
        private val log = KotlinLogging.logger {}
    }

    /**
     * Handles an incoming PaymentResponseDTO from the checkout session response queue.
     *
     * This method delegates processing to the PaymentWebhookService. It does NOT catch
     * exceptions thrown by the service. This is intentional and crucial: allowing the
     * exception to propagate signals a processing failure to RabbitMQ, which then
     * triggers the configured retry and/or dead-lettering mechanism. Swallowing the
     * exception here would lead to data loss.
     *
     * @param response The DTO containing the result of the checkout session.
     */
    @RabbitListener(queues = ["\${app.rabbitmq.checkout-session-response.queue}"]) // <-- CORRECTED QUEUE
    fun handleCheckoutSessionResponse(response: PaymentResponseDTO) {
        log.info { "Received checkout session response for orderId: ${response.orderId}, status: ${response.paymentStatus}" }

        // NO try-catch block here. Let the service handle its own errors and let
        // unrecoverable exceptions propagate to the listener container.
        paymentWebhookService.processCheckoutSessionResponse(response)

        log.info { "Successfully processed checkout session response for orderId: ${response.orderId}" }
    }
}