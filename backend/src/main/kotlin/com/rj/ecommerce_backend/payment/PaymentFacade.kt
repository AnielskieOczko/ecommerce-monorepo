package com.rj.ecommerce_backend.payment

import com.rj.ecommerce.api.shared.enums.PaymentStatus
import com.rj.ecommerce.api.shared.messaging.payment.CheckoutSessionDTO
import com.rj.ecommerce.api.shared.messaging.payment.PaymentStatusDTO
import com.rj.ecommerce_backend.messaging.payment.producer.PaymentMessageProducer
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.order.domain.OrderItem
import com.rj.ecommerce_backend.order.exception.OrderNotFoundException
import com.rj.ecommerce_backend.order.service.OrderService
import com.rj.ecommerce_backend.payment.gateway.PaymentGatewayResolver
import com.rj.ecommerce_backend.product.domain.Product
import com.rj.ecommerce_backend.security.SecurityContext
import com.rj.ecommerce_backend.security.exception.UserAuthenticationException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.IllegalStateException
import java.time.LocalDateTime

private val logger = KotlinLogging.logger { PaymentFacade::class }

@Service
@Transactional
class PaymentFacade(
    private val securityContext: SecurityContext,
    private val orderService: OrderService,
    private val paymentMessageProducer: PaymentMessageProducer,
    private val gatewayResolver: PaymentGatewayResolver // Inject the new resolver
) {

    fun createCheckoutSession(
        orderId: Long,
        successUrl: String,
        cancelUrl: String
    ): CheckoutSessionDTO {
        val userId = securityContext.getCurrentUser().id ?: throw UserAuthenticationException("User not authenticated.")

        // 1. All validation and business logic live here.
        val order = orderService.getOrderById(userId, orderId)
            ?: throw OrderNotFoundException("Order not found with ID: $orderId")

        if (isValidCheckoutSession(order)) {
            logger.info { "Found existing valid checkout session for order ID: $orderId. Session ID: ${order.paymentTransactionId}" }
            return CheckoutSessionDTO(
                orderId = order.id
                    ?: throw IllegalStateException("Order ID is null on a supposedly valid order."), // Should have ID
                paymentStatus = order.paymentStatus,
                sessionId = order.paymentTransactionId
                    ?: throw IllegalStateException("paymentTransactionId is null for an existing valid session on order $orderId."),
                sessionUrl = order.checkoutSessionUrl
                    ?: throw IllegalStateException("checkoutSessionUrl is null for an existing valid session on order $orderId."),
                expiresAt = order.checkoutSessionExpiresAt
            )
        }

        val orderPaymentMethod = requireNotNull(order.paymentMethod) {
            "Order ID ${order.id} does not have a payment method selected."
        }

        // The facade's job is now simple: ask the resolver for the correct gateway.
        val gateway = gatewayResolver.resolve(orderPaymentMethod)

        val paymentRequestDTO = gateway.buildPaymentRequest(order, successUrl, cancelUrl)

        // 4. Update our order state before sending the message.
        orderService.updatePaymentDetailsOnInitiation(order)

        // 5. Send the request to the Payment Microservice.
        paymentMessageProducer.sendCheckoutSessionRequest(paymentRequestDTO, order.id.toString())

        // 6. Return a PENDING response to the client.
        return CheckoutSessionDTO(
            orderId = order.id!!,
            paymentStatus = order.paymentStatus,
            sessionId = null, // Will be updated asynchronously
            sessionUrl = null, // Will be updated asynchronously
            expiresAt = null
        )
    }

    @Transactional(readOnly = true)
    fun getOrderPaymentStatus(orderId: Long): PaymentStatusDTO {
        // 1. Get current user's ID
        val currentUserId = securityContext.getCurrentUser().id
            ?: throw IllegalStateException("Authenticated user ID could not be determined.")
        // It's generally better to throw an authentication-related exception here,
        // e.g., NotAuthenticatedException or similar, rather than IllegalStateException.

        // 2. Fetch and Validate Order
        val order = orderService.getOrderById(currentUserId, orderId)
            ?: run {
                logger.warn { "Order not found with ID: $orderId for user ID: $currentUserId, or user does not have access." }
                throw OrderNotFoundException("Order not found with ID: $orderId, or access denied.") // Message reflects both possibilities
            }

        // 3. Construct and Return DTO
        val orderActualId = order.id
            ?: throw IllegalStateException("Order ID is null on a fetched order (ID: $orderId). Data consistency issue.")

        return PaymentStatusDTO(
            orderId = orderActualId,
            paymentStatus = order.paymentStatus,
            paymentTransactionId = order.paymentTransactionId,
            lastUpdate = order.updatedAt
        )
    }

    private fun isValidCheckoutSession(order: Order): Boolean {
        // Check for essential session details and a non-failed payment status
        if (order.paymentTransactionId == null ||
            order.checkoutSessionUrl == null ||
            order.paymentStatus == PaymentStatus.FAILED
        ) {
            logger.debug { "Checkout session for order ${order.id} is invalid due to missing details or failed status." }
            return false
        }

        // A session must have an expiration date to be considered valid for continuation.
        // If checkoutSessionExpiresAt is null, treat it as invalid.
        val expiresAt = order.checkoutSessionExpiresAt ?: run {
            logger.debug { "Checkout session for order ${order.id} is invalid because it has no expiration time." }
            return false
        }

        // A session is valid if its expiration time is after the current time.
        val isValid = expiresAt.isAfter(LocalDateTime.now())
        if (!isValid) {
            logger.info { "Checkout session for order ${order.id} has expired. Expiration: $expiresAt, Current time: ${LocalDateTime.now()}" }
        }
        return isValid
    }

    // Helper extension functions for logging nullable IDs (place in a suitable utility file)
    fun Order?.idStringForLog(): String = this?.id?.toString() ?: "UNKNOWN_ORDER_ID"
    fun OrderItem?.idStringForLog(): String = this?.id?.toString() ?: "UNKNOWN_ORDER_ITEM_ID"
    fun Product?.idStringForLog(): String = this?.id?.toString() ?: "UNKNOWN_PRODUCT_ID"

}