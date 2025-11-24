package com.rj.ecommerce_backend.payment

import com.rj.ecommerce_backend.api.shared.dto.payment.response.PaymentSessionResponse
import com.rj.ecommerce_backend.api.shared.dto.payment.response.PaymentStatusResponse
import com.rj.ecommerce_backend.api.shared.enums.CanonicalPaymentStatus
import com.rj.ecommerce_backend.api.shared.enums.PaymentMethod
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.order.exception.OrderNotFoundException
import com.rj.ecommerce_backend.order.service.OrderCommandService
import com.rj.ecommerce_backend.order.service.OrderQueryService
import com.rj.ecommerce_backend.payment.provider.PaymentProviderResolver
import com.rj.ecommerce_backend.security.SecurityContext
import com.rj.ecommerce_backend.security.exception.UserAuthenticationException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class PaymentFacade(
    private val securityContext: SecurityContext,
    private val orderCommandService: OrderCommandService,
    private val orderQueryService: OrderQueryService,
    private val paymentProviderResolver: PaymentProviderResolver
) {

    /**
     * Creates or retrieves a payment session for a given order.
     *
     * This method operates synchronously and has two main paths:
     * 1.  **Existing Valid Session:** If the order already has a valid, unexpired payment session URL,
     *     it returns the existing session details immediately without contacting the payment provider.
     * 2.  **New Session Creation:** If no valid session exists, it directly calls the configured payment
     *     provider (e.g., Stripe) to create a new checkout session. It then saves the session details
     *     (ID, URL, expiration) to the order and returns the complete response to the client in a single call.
     *
     * @param orderId The ID of the order to pay for.
     * @param successUrl The client-side URL to redirect to on successful payment.
     * @param cancelUrl The client-side URL to redirect to on payment cancellation.
     * @return A complete [PaymentSessionResponse] containing the checkout session URL.
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

        val orderPaymentMethod: PaymentMethod = requireNotNull(order.paymentMethod) {
            "Order ID ${order.id} does not have a payment method selected."
        }

        val provider = paymentProviderResolver.resolve(orderPaymentMethod)
        val sessionDetails = provider.initiatePayment(order, successUrl, cancelUrl)

        orderCommandService.updateOrderWithPaymentSession(
            order,
            sessionDetails.sessionId,
            sessionDetails.sessionUrl,
            sessionDetails.expiresAt)

        return PaymentSessionResponse(
            orderId = order.id!!,
            paymentStatus = order.paymentStatus,
            sessionId = sessionDetails.sessionId,
            sessionUrl = sessionDetails.sessionUrl,
            expiresAt = sessionDetails.expiresAt
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