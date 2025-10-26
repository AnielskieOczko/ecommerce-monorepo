package com.rj.ecommerce_backend.payment.provider

// Imports from both old classes
import com.rj.ecommerce_backend.api.shared.enums.CanonicalPaymentStatus
import com.rj.ecommerce_backend.api.shared.enums.OrderStatus
import com.rj.ecommerce_backend.api.shared.messaging.payment.common.PaymentLineItem
import com.rj.ecommerce_backend.api.shared.messaging.payment.request.PaymentInitiationRequest
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.order.service.OrderCommandService
import com.rj.ecommerce_backend.payment.config.StripeProperties
import com.rj.ecommerce_backend.payment.exception.PaymentLineItemCreationException
import com.rj.ecommerce_backend.payment.exception.PaymentProcessingException
import com.rj.ecommerce_backend.payment.model.PaymentSessionDetails
import com.stripe.exception.StripeException
import com.stripe.model.LineItem
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.collections.isNotEmpty
import kotlin.collections.map

private val logger = KotlinLogging.logger { StripePaymentProvider::class }

@Component // This is now our single component for Stripe
class StripePaymentProvider(
    private val orderCommandService: OrderCommandService,
    private val stripeProperties: StripeProperties
) : PaymentProvider {

    companion object {
        private const val SESSION_EXPIRATION_MINUTES = 30L
    }

    override fun initiatePayment(order: Order, successUrl: String, cancelUrl: String): PaymentSessionDetails {
        logger.info { "StripePaymentProvider: Initiating payment for Order ID ${order.id}" }

        // Step 1: Build the request DTO internally using the logic from the old StripeGateway.
        val request = buildPaymentInitiationRequest(order, successUrl, cancelUrl)

        // Step 2: Execute the payment creation using the logic from the old StripeProviderStrategy.
        try {
            validateRequest(request)
            val lineItems = buildStripeLineItemsFromRequest(request)
            val expiresAt = Instant.now().plusSeconds(SESSION_EXPIRATION_MINUTES * 60).epochSecond
            val paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCustomerEmail(request.customerEmail)
                .setSuccessUrl(request.successUrl)
                .setCancelUrl(request.cancelUrl)
                .addAllLineItem(lineItems)
                .setExpiresAt(expiresAt)
                .setPaymentIntentData(
                    SessionCreateParams.PaymentIntentData.builder()
                        .putAllMetadata(request.metadata).build()
                ).putAllMetadata(request.metadata)

            val session: Session = Session.create(paramsBuilder.build())
            logger.info { "Successfully created Stripe Session ID: ${session.id} for Order ID: ${request.orderId}" }

            return PaymentSessionDetails(
                sessionId = session.id,
                sessionUrl = session.url,
                expiresAt = LocalDateTime.ofInstant(Instant.ofEpochSecond(session.expiresAt), ZoneOffset.UTC)
            )
        } catch (e: StripeException) {
            logger.error(e) { "Stripe API error for Order ID ${request.orderId}: ${e.message}" }
            throw PaymentProcessingException("Stripe API Error: ${e.message}", e)
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during payment initiation for Order ID ${request.orderId}" }
            throw PaymentProcessingException("An unexpected internal error occurred.", e)
        }
    }

    private fun buildPaymentInitiationRequest(order: Order, successUrl: String, cancelUrl: String): PaymentInitiationRequest {
        val orderId = requireNotNull(order.id) { "Order ID cannot be null." }
        val user = requireNotNull(order.user) { "User on Order ID $orderId cannot be null." }

        if (order.orderItems.isEmpty()) {
            throw PaymentLineItemCreationException("Order ID $orderId has no items.")
        }
        val metadata = mapOf(
            "orderId" to orderId.toString(),
            "userId" to user.id.toString(),
            "customerEmail" to user.email
        )
        val lineItems = order.orderItems.map { orderItem ->
            val product = requireNotNull(orderItem.product)
            val itemPriceInCents = orderItem.price.multiply(BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP).longValueExact()

        }
        return PaymentInitiationRequest(
            orderId = orderId,
            customerEmail = user.email,
            successUrl = successUrl,
            cancelUrl = cancelUrl,
            lineItems = lineItems,
            metadata = metadata,
            providerIdentifier = getProviderIdentifier()
        )
    }

    /**
     * Implements the full lifecycle for processing a Stripe webhook.
     * 1. Verifies the webhook signature to ensure authenticity.
     * 2. Parses the event payload into a Stripe Event object.
     * 3. Determines the event type and dispatches to a specific handler.
     * 4. Translates the Stripe object into a canonical CheckoutSessionResponseDTO.
     * 5. Send the canonical DTO to the message queue.
     */
    override fun handleWebhook(payload: String, signature: String?) {
        logger.info { "StripeProviderStrategy: Handling incoming webhook." }

        val event: com.stripe.model.Event = try {
            // Step 1: Verify the signature. Throws an exception on failure.
            com.stripe.net.Webhook.constructEvent(
                payload, signature, stripeProperties.webhookSecret
            )
        } catch (e: com.stripe.exception.SignatureVerificationException) {
            logger.warn(e) { "Webhook signature verification failed. Request is invalid." }
            // Do not proceed. We simply drop invalid requests.
            return
        } catch (e: Exception) {
            logger.error(e) { "Failed to construct Stripe event from payload. Payload may be malformed." }
            // Do not proceed.
            return
        }

        // Step 2: Get the data object from the event.
        val stripeObject = event.dataObjectDeserializer.`object`.orElse(null)
        if (stripeObject == null) {
            logger.warn { "Stripe event ${event.id} received with no data object. Cannot process." }
            return
        }

        // Step 3 & 4: Dispatch based on an event type and translate to DTO.
        when (event.type) {
            "checkout.session.completed" -> processCheckoutSession(stripeObject as Session)
            "checkout.session.expired" -> processCheckoutSession(stripeObject as Session)
            // You can add more event handlers here, e.g., for charge.succeeded
            else -> logger.info { "Received unhandled Stripe event type: ${event.type}" }
        }
    }

    override fun getProviderIdentifier(): String {
        return "STRIPE"
    }

    /**
     * Processes a Stripe Session object from a webhook, translates it, and sends the message.
     */
    private fun processCheckoutSession(session: Session) {
        val orderId = session.metadata["orderId"]?.toLongOrNull()
        if (orderId == null) {
            logger.warn { "Received session webhook for ${session.id} with no valid orderId in metadata. Skipping." }
            return
        }

        logger.info { "Processing session webhook for Order ID: $orderId, Session ID: ${session.id}, Status: ${session.status}" }

        // --- Translation Logic ---
        val canonicalPaymentStatus = when (session.paymentStatus) {
            "paid" -> CanonicalPaymentStatus.SUCCEEDED
            "unpaid" -> CanonicalPaymentStatus.PENDING
            "no_payment_required" -> CanonicalPaymentStatus.SUCCEEDED
            else -> CanonicalPaymentStatus.UNKNOWN
        }

        val canonicalSessionStatus = when (session.status) {
            "open" -> CanonicalPaymentStatus.PENDING
            "complete" -> CanonicalPaymentStatus.SUCCEEDED
            "expired" -> CanonicalPaymentStatus.EXPIRED
            else -> CanonicalPaymentStatus.UNKNOWN
        }

        // Step 5: Send the canonical DTO to the message queue.
        orderCommandService.updateOrderStatus(
            orderId = orderId,
            newStatus = OrderStatus.PENDING
        )

        logger.info { "Successfully processed webhook and updated Order ID: $orderId" }
    }

    private fun buildStripeLineItemsFromRequest(request: PaymentInitiationRequest): List<SessionCreateParams.LineItem> {
        return request.lineItems.map { item: PaymentLineItem ->
            SessionCreateParams.LineItem.builder()
                .setQuantity(item.quantity.toLong())
                .setPriceData(
                    SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(item.currencyCode.lowercase())
                        .setUnitAmount(item.unitAmountCents)
                        .setProductData(
                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(item.name)
                                .setDescription(item.description)
                                .build()
                        ).build()
                ).build()
        }
    }


    private fun validateRequest(request: PaymentInitiationRequest) {
        require(request.successUrl.isNotBlank()) { "Success URL is required" }
        require(request.cancelUrl.isNotBlank()) { "Cancel URL is required" }
        require(request.lineItems.isNotEmpty()) { "At least one line item is required" }
    }
}