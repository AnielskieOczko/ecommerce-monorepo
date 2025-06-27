package com.rj.ecommerce_backend.payment

import com.rj.ecommerce.api.shared.messaging.payment.PaymentResponseDTO
import com.rj.ecommerce_backend.order.service.OrderCommandService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional // Annotation applies to the public method
class PaymentResponseProcessor(
    private val orderService: OrderCommandService,
    private val paymentNotificationService: PaymentNotificationService
) {

    companion object {
        // Use a more standard logger name for consistency
        private val log = KotlinLogging.logger {}
    }

    /**
     * Processes a checkout session response from a payment provider webhook.
     * 1. Updates the order status transactionally.
     * 2. Send a notification. A failure here does NOT roll back the order update.
     */
    fun processCheckoutSessionResponse(response: PaymentResponseDTO) {
        val orderId = response.orderId
        log.info { "Processing webhook response for orderId: $orderId, status: ${response.paymentStatus}" }

        try {
            // --- Step 1: Critical Transactional Update ---
            orderService.updateOrderWithCheckoutSession(response)

            // --- Step 2: Non-Critical Notification ---
            try {
                paymentNotificationService.sendPaymentNotification(response)
                log.info { "Successfully sent payment notification for orderId: $orderId" }
            } catch (notificationError: Exception) {
                log.error(notificationError) { "Failed to send payment notification for orderId: $orderId after successful update." }
            }

        } catch (e: Exception) {
            log.error(e) { "Failed to process webhook for orderId: $orderId. Sending error notification." }

            // Attempt to send an error notification as a best-effort.
            // This should not throw an exception that would mask the original one.
            sendErrorNotificationSafely(response, e)

            // Re-throw the original, critical exception to ensure the transaction is rolled back
            // and the caller (e.g., the webhook listener) knows the processing failed.
            throw e
        }
    }

    /**
     * Sends an error notification in a safe way, ensuring it doesn't throw an exception itself.
     */
    private fun sendErrorNotificationSafely(response: PaymentResponseDTO, originalError: Exception) {
        try {
            paymentNotificationService.sendPaymentErrorNotification(response, originalError)
        } catch (notificationError: Exception) {
            log.error(notificationError) { "CRITICAL: Failed to send ERROR notification for orderId: ${response.orderId} after processing failure." }
        }
    }
}