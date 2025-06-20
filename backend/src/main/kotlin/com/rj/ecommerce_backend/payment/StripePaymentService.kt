package com.rj.ecommerce_backend.payment

import com.rj.ecommerce.api.shared.enums.PaymentStatus
import com.rj.ecommerce.api.shared.messaging.payment.CheckoutSessionDTO
import com.rj.ecommerce.api.shared.messaging.payment.PaymentLineItemDTO
import com.rj.ecommerce.api.shared.messaging.payment.PaymentRequestDTO
import com.rj.ecommerce.api.shared.messaging.payment.PaymentStatusDTO
import com.rj.ecommerce_backend.messaging.payment.producer.PaymentMessageProducer
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.order.domain.OrderItem
import com.rj.ecommerce_backend.order.exceptions.OrderNotFoundException
import com.rj.ecommerce_backend.order.service.OrderService
import com.rj.ecommerce_backend.product.domain.Product
import com.rj.ecommerce_backend.security.SecurityContext
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.IllegalStateException
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

@Service
@Transactional
class StripePaymentService(
    private val orderService: OrderService,
    private val paymentMessageProducer: PaymentMessageProducer,
    private val securityContext: SecurityContext
) {
    companion object {
        private val logger = KotlinLogging.logger { }
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


    @Transactional
    fun createOrGetCheckoutSession(
        userId: Long,
        orderId: Long,
        successUrl: String,
        cancelUrl: String
    ): CheckoutSessionDTO {

        // 1. Fetch and Validate Order
        val order = orderService.getOrderById(userId, orderId)
            ?: run {
                logger.warn { "Order not found with ID: $orderId when attempting to create/get checkout session for user ID: $userId" }
                throw OrderNotFoundException("Order not found with ID: $orderId")
            }

        // Verify the order belongs to the user
        val orderUserId = order.user?.id
            ?: throw IllegalStateException("User associated with order ID $orderId is null.") // Should not happen if data is consistent

        if (orderUserId != userId) {
            logger.warn { "User $userId attempted to access order $orderId belonging to user $orderUserId." }
            // Throwing OrderNotFoundException here might be misleading to the attacking user.
            // A generic "Access Denied" or "Forbidden" (HttpStatus.FORBIDDEN) might be better,
            // or a more specific AuthorizationFailedException. For now, sticking to original.
            throw OrderNotFoundException("Order not found with ID: $orderId for the current user.") // Slightly modified message for context
        }

        // 2. Check for Existing Valid Session
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

        logger.info { "No existing valid session found for order ID: $orderId. Creating a new one for user ID: $userId." }

        // 3. Create New Checkout Session
        val paymentRequestDTO = try {
            buildCheckoutRequest(order, successUrl, cancelUrl)
        } catch (e: PaymentLineItemCreationException) {
            logger.error(e) { "Failed to build checkout request for order ID $orderId due to invalid line items: ${e.message}" }
            throw e
        }

        orderService.updatePaymentDetailsOnInitiation(order)
        logger.debug { "Updated payment details on initiation for order ID: $orderId. New status: ${order.paymentStatus}" }

        paymentMessageProducer.sendCheckoutSessionRequest(
            paymentRequestDTO,
            order.id.toString()
        )
        logger.info { "Sent checkout session request to message producer for order ID: ${paymentRequestDTO.orderId}." }

        return CheckoutSessionDTO(
            orderId = order.id ?: throw IllegalStateException("Order ID became null unexpectedly."),
            paymentStatus = order.paymentStatus,
            sessionId = order.paymentTransactionId,
            sessionUrl = order.checkoutSessionUrl,
            expiresAt = order.checkoutSessionExpiresAt
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


    private fun buildCheckoutRequest(
        order: Order,
        successUrl: String,
        cancelUrl: String
    ): PaymentRequestDTO {
        val currentOrderId = order.id
            ?: throw PaymentLineItemCreationException("Order ID cannot be null to build checkout request for order: ${order.idStringForLog()}") // Assuming a helper for logging
        val orderUser = order.user
            ?: throw PaymentLineItemCreationException("User on order ID $currentOrderId cannot be null.")
        val currentUserIdOnOrder = orderUser.id
            ?: throw PaymentLineItemCreationException("User ID on order ID $currentOrderId cannot be null.")
        val currentUserEmail = orderUser.email.value // Assuming email and value are non-null

        val metadata: Map<String, String> = mapOf(
            "orderId" to currentOrderId.toString(),
            "userId" to currentUserIdOnOrder.toString(),
            "customerEmail" to currentUserEmail
        )

        val orderCurrencyCode = order.currency.name

        if (order.orderItems.isEmpty()) {
            throw PaymentLineItemCreationException(
                "Order ID $currentOrderId has no order items. Proceeding to create payment request, possibly for $0 or non-item charges."
            )
        }

        val lineItems: List<PaymentLineItemDTO> = order.orderItems.map { orderItem ->
            val orderItemIdForLog = orderItem.idStringForLog() // Helper for logging

            val product = orderItem.product
                ?: throw PaymentLineItemCreationException("OrderItem $orderItemIdForLog in order $currentOrderId has no associated product.")

            val productIdForLog = product.idStringForLog()

            val productName = product.name?.value
                ?: throw PaymentLineItemCreationException("Product $productIdForLog (for OrderItem $orderItemIdForLog) has no name.")

            val productDescriptionString = product.description?.value

            val itemPriceBigDecimal = orderItem.price
                ?: throw PaymentLineItemCreationException("OrderItem $orderItemIdForLog (Product $productIdForLog) in order $currentOrderId has no price.")

            if (itemPriceBigDecimal.compareTo(BigDecimal.ZERO) < 0) {
                throw PaymentLineItemCreationException("OrderItem $orderItemIdForLog (Product $productIdForLog) in order $currentOrderId has a negative price: $itemPriceBigDecimal.")
            }

            val itemPriceInCents = itemPriceBigDecimal.multiply(BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact() // This will also throw if conversion to long is not exact after scaling

            if (orderItem.quantity < 1) {
                throw PaymentLineItemCreationException("OrderItem $orderItemIdForLog (Product $productIdForLog) in order $currentOrderId has invalid quantity: ${orderItem.quantity}.")
            }

            try {
                PaymentLineItemDTO(
                    name = productName,
                    description = productDescriptionString,
                    unitAmountCents = itemPriceInCents,
                    quantity = orderItem.quantity,
                    currencyCode = orderCurrencyCode
                )
            } catch (e: IllegalArgumentException) {
                throw PaymentLineItemCreationException(
                    "Failed to create PaymentLineItemDTO for OrderItem $orderItemIdForLog (Product $productIdForLog) due to: ${e.message}",
                    e
                )
            }
        }

        // This check might now be redundant if orderItems.isEmpty() is handled above,
        // as any invalid item during mapping would have thrown an exception.
        // However, it's a good safeguard if the logic above changes.
        if (order.orderItems.isNotEmpty() && lineItems.isEmpty()) {
            // This state should ideally not be reached if exceptions are thrown during mapping.
            throw PaymentLineItemCreationException("Order ID $currentOrderId has order items, but resulted in no valid payment line items. This indicates an unexpected issue.")
        }

        return PaymentRequestDTO(
            orderId = currentOrderId,
            customerEmail = currentUserEmail,
            successUrl = successUrl,
            cancelUrl = cancelUrl,
            lineItems = lineItems,
            metadata = metadata
        )
    }

    // Helper extension functions for logging nullable IDs (place in a suitable utility file)
    fun Order?.idStringForLog(): String = this?.id?.toString() ?: "UNKNOWN_ORDER_ID"
    fun OrderItem?.idStringForLog(): String = this?.id?.toString() ?: "UNKNOWN_ORDER_ITEM_ID"
    fun Product?.idStringForLog(): String = this?.id?.toString() ?: "UNKNOWN_PRODUCT_ID"
}

