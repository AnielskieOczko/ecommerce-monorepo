package com.rj.ecommerce_backend.payment

import com.rj.ecommerce.api.shared.enums.CanonicalPaymentStatus
import com.rj.ecommerce.api.shared.messaging.payment.PaymentResponseDTO
import com.rj.ecommerce_backend.events.payment.PaymentFailedEvent
import com.rj.ecommerce_backend.events.payment.PaymentSucceededEvent
import com.rj.ecommerce_backend.order.service.OrderCommandService
import com.rj.ecommerce_backend.order.service.OrderQueryService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentResponseProcessor(
    private val orderCommandService: OrderCommandService,
    private val orderQueryService: OrderQueryService,
    private val eventPublisher: ApplicationEventPublisher
) {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    @Transactional
    fun processCheckoutSessionResponse(response: PaymentResponseDTO) {
        log.info { "Processing webhook response for orderId: ${response.orderId}, status: ${response.paymentStatus}" }

        try {
            // This call now only handles the state change of the order.
            orderCommandService.updateOrderWithCheckoutSession(response)

            // After the state is successfully updated and committed, publish an event.
            // We need to re-fetch the order to ensure we have the fully updated state.
            val updatedOrder = orderQueryService.findOrderEntityById(response.orderId)
                ?: run {
                    log.error { "Could not re-fetch order ${response.orderId} after update. Cannot publish event." }
                    return
                }

            when (response.paymentStatus) {
                CanonicalPaymentStatus.SUCCEEDED -> {
                    eventPublisher.publishEvent(PaymentSucceededEvent(this, updatedOrder, response))
                }
                CanonicalPaymentStatus.FAILED, CanonicalPaymentStatus.EXPIRED -> {
                    eventPublisher.publishEvent(PaymentFailedEvent(this, updatedOrder, response))
                }
                else -> {
                    log.info { "No event published for payment status: ${response.paymentStatus}" }
                }
            }
        } catch (e: Exception) {
            log.error(e) { "Failed to process webhook for orderId: ${response.orderId}." }
            // No need to send an error notification here, as the transaction will roll back,
            // and the message will likely be retried or sent to a DLQ.
            throw e
        }
    }
}