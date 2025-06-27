package com.rj.ecommerce_backend.payment

import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.enums.CanonicalPaymentStatus
import com.rj.ecommerce.api.shared.enums.Currency
import com.rj.ecommerce.api.shared.enums.NotificationTemplate
import com.rj.ecommerce.api.shared.messaging.contract.MessageVersioning.CURRENT_VERSION
import com.rj.ecommerce.api.shared.messaging.email.PaymentEmailRequestDTO
import com.rj.ecommerce.api.shared.messaging.payment.PaymentResponseDTO
import com.rj.ecommerce_backend.messaging.email.client.EmailServiceClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Service responsible for sending payment-related notifications via email.
 * This service is designed to work with the extended `PaymentResponseDTO`.
 */
@Service
class PaymentNotificationService(
    private val emailServiceClient: EmailServiceClient
) {

    companion object {
        private val log = LoggerFactory.getLogger(PaymentNotificationService::class.java)
        private const val ADMIN_EMAIL = "admin@yourstore.com"
        private const val SUPPORT_EMAIL = "support@yourstore.com"
        private const val PAYMENT_HELP_URL = "https://yourstore.com/payment-help"
        private val CENTS_TO_MAIN_UNIT_DIVISOR = BigDecimal("100.0")
    }

    /**
     * Routes the notification request based on the payment status from the PaymentResponseDTO.
     *
     * @param response An extended DTO containing all necessary information about the payment event.
     */
    fun sendPaymentNotification(response: PaymentResponseDTO) {
        log.info("Sending payment notification for order: ${response.orderId}")

        when (response.paymentStatus) {
            CanonicalPaymentStatus.SUCCEEDED -> sendPaymentSuccessNotification(response)
            CanonicalPaymentStatus.FAILED -> sendPaymentFailureNotification(response)
            else -> log.warn("Unhandled payment status: ${response.paymentStatus} for order: ${response.orderId}")
        }
    }

    private fun sendPaymentSuccessNotification(response: PaymentResponseDTO) {
        log.info("Sending payment success notification for order: ${response.orderId}")

        val emailRequest = PaymentEmailRequestDTO(
            version = CURRENT_VERSION,
            to = response.customerEmail,
            template = NotificationTemplate.PAYMENT_CONFIRMATION,
            orderId = response.orderId,
            paymentId = response.sessionId,
            paymentStatus = "SUCCEEDED",
            paymentAmount = createMoney(response),
            additionalData = createAdditionalData(response),
            messageId = UUID.randomUUID().toString(),
            correlationId = response.correlationId,
            timestamp = LocalDateTime.now()
        )

        emailServiceClient.sendEmailRequest(emailRequest)
    }

    private fun sendPaymentFailureNotification(response: PaymentResponseDTO) {
        log.info("Sending payment failure notification for order: ${response.orderId}")

        // Prepare extra data for the email template
        val extraData = mapOf(
            "retryUrl" to response.checkoutUrl,
            "supportEmail" to SUPPORT_EMAIL
        )

        val emailRequest = PaymentEmailRequestDTO(
            version = CURRENT_VERSION,
            to = response.customerEmail,
            template = NotificationTemplate.PAYMENT_FAILED,
            orderId = response.orderId,
            paymentId = response.sessionId,
            paymentStatus = "FAILED",
            paymentAmount = createMoney(response),
            additionalData = createAdditionalData(response, extraData),
            messageId = UUID.randomUUID().toString(),
            correlationId = response.correlationId,
            timestamp = LocalDateTime.now()
        )

        emailServiceClient.sendEmailRequest(emailRequest)
    }

    /**
     * Sends error notifications when an unhandled exception occurs.
     *
     * @param response The payment response data available at the time of the error.
     * @param e The exception that occurred.
     */
    fun sendPaymentErrorNotification(response: PaymentResponseDTO, e: Exception) {
        log.error("Sending payment error notifications for order: ${response.orderId}", e)

        // Admin notification
        val adminNotification = PaymentEmailRequestDTO(
            version = CURRENT_VERSION,
            to = ADMIN_EMAIL,
            template = NotificationTemplate.PAYMENT_ERROR_ADMIN,
            orderId = response.orderId,
            paymentId = response.sessionId,
            paymentStatus = "ERROR",
            paymentAmount = createMoney(response),
            additionalData = createAdditionalData(
                response, mapOf(
                    "errorMessage" to (e.message ?: "Unknown error"),
                    "timestamp" to LocalDateTime.now().toString()
                )
            ),
            messageId = UUID.randomUUID().toString(),
            correlationId = response.correlationId,
            timestamp = LocalDateTime.now()
        )

        // Customer notification
        val customerNotification = PaymentEmailRequestDTO(
            version = CURRENT_VERSION,
            to = response.customerEmail,
            template = NotificationTemplate.PAYMENT_ERROR_CUSTOMER,
            orderId = response.orderId,
            paymentId = response.sessionId,
            paymentStatus = "ERROR",
            paymentAmount = createMoney(response),
            additionalData = createAdditionalData(
                response, mapOf(
                    "supportEmail" to SUPPORT_EMAIL,
                    "helpUrl" to PAYMENT_HELP_URL
                )
            ),
            messageId = UUID.randomUUID().toString(),
            correlationId = response.correlationId,
            timestamp = LocalDateTime.now()
        )

        emailServiceClient.sendEmailRequest(adminNotification)
        emailServiceClient.sendEmailRequest(customerNotification)
    }

    // --- Helper Methods ---

    /**
     * Safely creates a `Money` object from the response DTO.
     * Converts amount from cents to the main currency unit.
     */
    private fun createMoney(response: PaymentResponseDTO): Money? {
        return response.amountTotal?.let { amount ->
            Money(
                amount = BigDecimal(amount).divide(CENTS_TO_MAIN_UNIT_DIVISOR),
                currencyCode = try {
                    // Make the currency lookup robust against case and nulls
                    Currency.valueOf((response.currency ?: "USD").uppercase())
                } catch (e: IllegalArgumentException) {
                    log.warn("Invalid currency code '${response.currency}' for order ${response.orderId}. Defaulting to USD.")
                    Currency.USD
                }
            )
        }
    }

    /**
     * Creates a map of additional data by merging the response's metadata
     * with any extra data provided.
     */
    private fun createAdditionalData(
        response: PaymentResponseDTO,
        extraData: Map<String, Any> = emptyMap()
    ): Map<String, Any> {
        return buildMap {
            put("orderId", response.orderId)

            // Use the 'metadata' field from the DTO
            response.metadata?.let { putAll(it) }

            // Add any other specific data, overwriting if keys conflict
            putAll(extraData)
        }
    }
}