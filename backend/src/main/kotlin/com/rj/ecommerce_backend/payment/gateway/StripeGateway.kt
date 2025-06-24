package com.rj.ecommerce_backend.payment.gateway

import com.rj.ecommerce.api.shared.messaging.payment.PaymentLineItemDTO
import com.rj.ecommerce.api.shared.messaging.payment.PaymentRequestDTO
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.payment.exception.PaymentLineItemCreationException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

private val logger = KotlinLogging.logger { StripeGateway::class }

@Component
class StripeGateway : PaymentGateway {
    /**
     * Builds a Stripe-specific payment request from a given order.
     *
     * @param order The validated Order entity containing all necessary details.
     * @param successUrl The URL to redirect the user to on successful payment.
     * @param cancelUrl The URL to redirect the user to on payment cancellation.
     * @return A fully constructed PaymentRequestDTO.
     * @throws PaymentLineItemCreationException if the order data is invalid or
     *         insufficient to create a valid payment request.
     */
    override fun buildPaymentRequest(order: Order, successUrl: String, cancelUrl: String): PaymentRequestDTO {
        logger.debug { "Building Stripe payment request for Order ID: ${order.id}" }

        // --- 1. Pre-condition Checks: Ensure the Order object is valid for payment ---
        val orderId = requireNotNull(order.id) {
            "Order ID cannot be null to build a payment request."
        }
        val user = requireNotNull(order.user) {
            "User on Order ID $orderId cannot be null."
        }
        val userEmail = user.email.value

        if (order.orderItems.isEmpty()) {
            throw PaymentLineItemCreationException("Order ID $orderId has no items. Cannot create a payment session.")
        }

        // --- 2. Prepare Metadata ---
        // This metadata will be passed through to the Payment Microservice and can be
        // useful for linking events back to the main application's data.
        val metadata = mapOf(
            "orderId" to orderId.toString(),
            "userId" to user.id.toString(),
            "customerEmail" to userEmail
        )

        // --- 3. Build Line Items ---
        // This is the core translation logic. Each OrderItem becomes a PaymentLineItemDTO.
        val lineItems = order.orderItems.map { orderItem ->
            val product = requireNotNull(orderItem.product) {
                "OrderItem ID ${orderItem.id} in Order ID $orderId has no associated product."
            }
            val productName = requireNotNull(product.name.value) {
                "Product ID ${product.id} has no name."
            }
            val itemPrice = requireNotNull(orderItem.price) {
                "OrderItem ID ${orderItem.id} has no price."
            }

            if (itemPrice < BigDecimal.ZERO) {
                throw PaymentLineItemCreationException("OrderItem ID ${orderItem.id} has a negative price: $itemPrice.")
            }
            if (orderItem.quantity < 1) {
                throw PaymentLineItemCreationException("OrderItem ID ${orderItem.id} has an invalid quantity: ${orderItem.quantity}.")
            }

            // Stripe requires the amount in the smallest currency unit (e.g., cents).
            val itemPriceInCents = itemPrice.multiply(BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact()

            PaymentLineItemDTO(
                name = productName,
                description = product.description?.value,
                unitAmountCents = itemPriceInCents,
                quantity = orderItem.quantity,
                currencyCode = order.currency.name // Assumes all items have the same currency as the order.
            )
        }

        // --- 4. Construct and Return the Final DTO ---
        val request = PaymentRequestDTO(
            orderId = orderId,
            customerEmail = userEmail,
            successUrl = successUrl,
            cancelUrl = cancelUrl,
            lineItems = lineItems,
            metadata = metadata,
            providerIdentifier = "STRIPE"
        )

        logger.info { "Successfully built Stripe PaymentRequestDTO for Order ID: $orderId" }
        return request
    }

    override fun getGatewayIdentifier(): String {
        // This is the key we will use in our YAML configuration.
        return "STRIPE_GATEWAY"
    }
}