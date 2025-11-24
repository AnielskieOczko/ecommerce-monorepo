package com.rj.ecommerce_backend.payment.provider

import com.rj.ecommerce_backend.api.shared.enums.CanonicalPaymentStatus
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.order.service.OrderCommandService
import com.rj.ecommerce_backend.payment.config.PaymentProperties
import com.rj.ecommerce_backend.payment.config.ProviderConfig
import com.rj.ecommerce_backend.payment.exception.PaymentLineItemCreationException
import com.rj.ecommerce_backend.payment.exception.PaymentProcessingException
import com.rj.ecommerce_backend.payment.model.PaymentSessionDetails
import com.stripe.exception.StripeException
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.collections.map

private val logger = KotlinLogging.logger { }

@Component
class StripePaymentProvider(
    private val orderCommandService: OrderCommandService,
    private val paymentProperties: PaymentProperties
) : PaymentProvider {

    private lateinit var stripeConfig: ProviderConfig

    companion object {
        private const val SESSION_EXPIRATION_MINUTES = 30L
    }

    /**
     * This method runs once at startup to validate and extract the Stripe-specific
     * configuration, ensuring the provider fails fast if not configured correctly.
     */
    @PostConstruct
    fun init() {
        // Find our specific config from the map
        val config = paymentProperties.providers["stripe"]
            ?: throw IllegalStateException("Configuration for 'stripe' provider is missing in application.yml.")

        // If the provider is enabled, we MUST have an API key and webhook secret.
        if (config.enabled) {
            require(!config.apiKey.isNullOrBlank()) { "Stripe provider is enabled, but 'apiKey' is missing in application.yml." }
            require(!config.webhookSecret.isNullOrBlank()) { "Stripe provider is enabled, but 'webhookSecret' is missing in application.yml." }
        }

        this.stripeConfig = config
    }

    override fun initiatePayment(order: Order, successUrl: String, cancelUrl: String): PaymentSessionDetails {

        val orderId = requireNotNull(order.id) { "Order must have an ID to initiate payment." }
        logger.info { "StripePaymentProvider: Initiating payment for Order ID $orderId" }

        try {
            // --- 1. VALIDATE PRECONDITIONS ---
            val user = requireNotNull(order.user) { "Order must have an associated user." }
            if (order.orderItems.isEmpty()) {
                throw PaymentLineItemCreationException("Order ID $orderId has no items. Cannot create a payment session.")
            }
            require(successUrl.isNotBlank()) { "Success URL is required." }
            require(cancelUrl.isNotBlank()) { "Cancel URL is required." }

            // --- 2. PREPARE METADATA ---
            val metadata = mapOf(
                "orderId" to orderId.toString(),
                "userId" to user.id.toString(),
                "customerEmail" to user.email.value
            )

            // --- 3. BUILD STRIPE-SPECIFIC LINE ITEMS ---
            // This logic is moved directly from the old StripeGateway/buildStripeLineItemsFromRequest
            val lineItems = order.orderItems.map { orderItem ->
                val product = requireNotNull(orderItem.product) { "OrderItem ${orderItem.id} is missing product data." }
                val unitPrice = requireNotNull(orderItem.price) { "OrderItem ${orderItem.id} is missing price data." }

                // Stripe requires the amount in the smallest currency unit (e.g., cents).
                val itemPriceInCents = unitPrice.multiply(BigDecimal("100"))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValueExact()

                SessionCreateParams.LineItem.builder()
                    .setQuantity(orderItem.quantity.toLong())
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(order.currency.name.lowercase())
                            .setUnitAmount(itemPriceInCents)
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(product.name.value)
                                    .setDescription(product.description?.value)
                                    .build()
                            ).build()
                    ).build()
            }

            // --- 4. BUILD THE MAIN SESSION CREATION PARAMETERS ---
            val expiresAt = Instant.now().plusSeconds(SESSION_EXPIRATION_MINUTES * 60).epochSecond
            val paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCustomerEmail(user.email.value)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addAllLineItem(lineItems)
                .setExpiresAt(expiresAt)
                .putAllMetadata(metadata) // Set metadata for the session
                .setPaymentIntentData( // Also set metadata for the payment intent for webhook retrieval
                    SessionCreateParams.PaymentIntentData.builder()
                        .putAllMetadata(metadata)
                        .build()
                )

            // --- 5. CALL THE STRIPE API ---
            val session: Session = Session.create(paramsBuilder.build())
            logger.info { "Successfully created Stripe Session ID: ${session.id} for Order ID: $orderId" }

            // --- 6. RETURN THE RESULT DIRECTLY ---
            return PaymentSessionDetails(
                sessionId = session.id,
                sessionUrl = requireNotNull(session.url) { "Stripe session URL cannot be null." },
                expiresAt = LocalDateTime.ofInstant(Instant.ofEpochSecond(session.expiresAt), ZoneOffset.UTC)
            )

        } catch (e: StripeException) {
            logger.error(e) { "Stripe API error for Order ID $orderId: ${e.message}" }
            // Re-throw as our domain-specific exception
            throw PaymentProcessingException("Stripe API Error: ${e.message}", e)
        } catch (e: Exception) {
            // Catch other exceptions like IllegalArgumentException from requireNotNull
            logger.error(e) { "Unexpected error during payment initiation for Order ID $orderId" }
            throw PaymentProcessingException("An unexpected internal error occurred: ${e.message}", e)
        }
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

        // 1. Check if the provider is even active. If not, do nothing.
        if (!stripeConfig.enabled) {
            logger.warn { "Received a Stripe webhook, but the Stripe provider is disabled in configuration. Ignoring." }
            return
        }

        val secret = requireNotNull(stripeConfig.webhookSecret) { "Stripe webhook secret is not configured." }

        val event: com.stripe.model.Event = try {
            // Step 1: Verify the signature. Throws an exception on failure.
            com.stripe.net.Webhook.constructEvent(
                payload, signature, secret
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
        val newPaymentStatus = when (session.status) {
            "complete" -> CanonicalPaymentStatus.SUCCEEDED
            "expired" -> CanonicalPaymentStatus.EXPIRED
            // "open" can be treated as PENDING, but we often don't need to process it.
            // We only care about the final states.
            else -> {
                logger.info { "Received non-terminal webhook status '${session.status}' for Order ID $orderId. Ignoring." }
                return // Exit early for statuses we don't care about
            }
        }

        // --- DIRECT SERVICE CALL with CORRECT status ---
        // Use the method we created specifically for this purpose.
        orderCommandService.updateOrderStatusFromPayment(
            orderId = orderId,
            newStatus = newPaymentStatus,
            transactionId = session.id // Also pass the transaction ID for auditing
        )

        logger.info { "Successfully processed webhook and updated Order ID: $orderId to status $newPaymentStatus" }
    }

}