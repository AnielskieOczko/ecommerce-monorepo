package com.rj.ecommerce_backend.payment

import com.rj.ecommerce.api.shared.dto.payment.response.PaymentSessionResponse
import com.rj.ecommerce.api.shared.dto.payment.response.PaymentStatusResponse
import com.rj.ecommerce.api.shared.enums.CanonicalPaymentStatus
import com.rj.ecommerce_backend.messaging.payment.producer.PaymentMessageProducer
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.order.exception.OrderNotFoundException
import com.rj.ecommerce_backend.order.service.OrderCommandService
import com.rj.ecommerce_backend.order.service.OrderQueryService
import com.rj.ecommerce_backend.payment.gateway.PaymentGatewayResolver
import com.rj.ecommerce_backend.security.SecurityContext
import com.rj.ecommerce_backend.security.exception.UserAuthenticationException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val logger = KotlinLogging.logger { PaymentFacade::class }

@Service
@Transactional
class PaymentFacade(
    private val securityContext: SecurityContext,
    private val orderCommandService: OrderCommandService,
    private val orderQueryService: OrderQueryService,
    private val paymentMessageProducer: PaymentMessageProducer,
    private val gatewayResolver: PaymentGatewayResolver
) {

    /**
     * Creates or retrieves a payment session for a given order.
     *
     * This method has two paths:
     * 1.  **Existing Valid Session:** If the order already has a valid, unexpired payment session URL,
     *     it returns the existing session details immediately.
     * 2.  **New Session:** If no valid session exists, it initiates a new one by sending an
     *     asynchronous request to the payment service. It then **synchronously returns a PENDING response**.
     *     The client must then poll the `getOrderPaymentStatus` endpoint to get the actual session URL
     *     once it has been processed.
     *
     * @param orderId The ID of the order to pay for.
     * @param successUrl The client-side URL to redirect to on successful payment.
     * @param cancelUrl The client-side URL to redirect to on payment cancellation.
     * @return A [PaymentSessionResponse] containing either the existing session details or a pending status.
     */
    fun createCheckoutSession(
        orderId: Long,
        successUrl: String,
        cancelUrl: String
    ): PaymentSessionResponse {
        securityContext.getCurrentUser().id
            ?: throw UserAuthenticationException("User not authenticated.")

        val order = orderQueryService.findOrderEntityById(orderId)
            ?: throw OrderNotFoundException("Order not found with ID: $orderId")

        if (isValidCheckoutSession(order)) {
            logger.info { "Found existing valid checkout session for order ID: $orderId. Session ID: ${order.paymentTransactionId}" }
            return PaymentSessionResponse(
                orderId = order.id!!,
                paymentStatus = order.paymentStatus,
                sessionId = order.paymentTransactionId,
                sessionUrl = order.checkoutSessionUrl,
                expiresAt = order.checkoutSessionExpiresAt
            )
        }

        val orderPaymentMethod = requireNotNull(order.paymentMethod) {
            "Order ID ${order.id} does not have a payment method selected."
        }

        val gateway = gatewayResolver.resolve(orderPaymentMethod)
        val paymentRequestDTO = gateway.buildPaymentRequest(order, successUrl, cancelUrl)

        orderCommandService.updatePaymentDetailsOnInitiation(order)
        paymentMessageProducer.sendCheckoutSessionRequest(paymentRequestDTO, order.id.toString())

        // REFACTOR: Return the correct PENDING response DTO to the client.
        // The client understands that sessionId and sessionUrl will be null initially
        // and will poll for the updated status.
        return PaymentSessionResponse(
            orderId = order.id!!,
            paymentStatus = order.paymentStatus,
            sessionId = null, // Will be populated asynchronously
            sessionUrl = null, // Will be populated asynchronously
            expiresAt = null
        )
    }

    /**
     * Retrieves the current payment status for a given order.
     *
     * @param orderId The ID of the order to check.
     * @return A [PaymentStatusResponse] with the latest payment details.
     */
    @Transactional(readOnly = true)
    fun getOrderPaymentStatus(orderId: Long): PaymentStatusResponse {
        val currentUserId = securityContext.getCurrentUser().id
            ?: throw UserAuthenticationException("Authenticated user ID could not be determined.")

        // REFACTOR: This logic was already correct, but we confirm it uses the new DTO.
        val order = orderQueryService.getOrderEntityByIdForUser(currentUserId, orderId)
            ?: throw OrderNotFoundException("Order not found with ID: $orderId, or access denied.")

        return PaymentStatusResponse(
            orderId = order.id!!,
            paymentStatus = order.paymentStatus,
            paymentTransactionId = order.paymentTransactionId,
            lastUpdate = order.updatedAt
        )
    }

    private fun isValidCheckoutSession(order: Order): Boolean {
        if (order.paymentTransactionId == null ||
            order.checkoutSessionUrl == null ||
            order.paymentStatus == CanonicalPaymentStatus.FAILED
        ) {
            logger.debug { "Checkout session for order ${order.id} is invalid due to missing details or failed status." }
            return false
        }

        val expiresAt = order.checkoutSessionExpiresAt ?: return false
        val isValid = expiresAt.isAfter(LocalDateTime.now())

        if (!isValid) {
            logger.info { "Checkout session for order ${order.id} has expired. Expiration: $expiresAt, Current time: ${LocalDateTime.now()}" }
        }
        return isValid
    }
}