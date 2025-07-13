package com.rj.payment_service.service

import com.rj.ecommerce.api.shared.enums.CanonicalPaymentStatus
import com.rj.ecommerce.api.shared.messaging.payment.request.PaymentInitiationRequest
import com.rj.ecommerce.api.shared.messaging.payment.response.PaymentInitiationResponse
import com.rj.payment_service.config.StripeProperties
import com.rj.payment_service.producer.MessageProducer
import com.stripe.exception.StripeException
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

private val logger = KotlinLogging.logger { StripeProviderStrategy::class }

@Component
class StripeProviderStrategy(
    private val messageProducer: MessageProducer,
    private val stripeProperties: StripeProperties
) : PaymentProviderStrategy {

    companion object {
        // Stripe sessions must expire within 24 hours. 30 minutes is a safe default.
        private const val SESSION_EXPIRATION_MINUTES = 30L
    }

    /**
     * Implements the full lifecycle for initiating a Stripe payment.
     * 1. Validates the incoming request.
     * 2. Build the Stripe-specific API parameters.
     * 3. Call the Stripe API to create a Checkout Session.
     * 4. On success, builds and sends a detailed success response DTO.
     * 5. On failure, catches the exception, builds, and sends a detailed error response DTO.
     */
    override fun initiatePayment(request: PaymentInitiationRequest, correlationId: String?) {
        logger.info { "StripeProviderStrategy: Initiating payment for Order ID ${request.orderId}" }
        try {
            // Step 1: Validate the request data before making any API calls.
            validateRequest(request)

            // Step 2: Build the Stripe-specific line items.
            val lineItems = buildStripeLineItemsFromRequest(request)

            // Step 3: Build the main session creation parameters.
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
                        .putAllMetadata(request.metadata)
                        .build()
                )
                .putAllMetadata(request.metadata)

            // Step 4: Call the Stripe API.
            val session: Session = Session.create(paramsBuilder.build())
            logger.info { "Successfully created Stripe Session ID: ${session.id} for Order ID: ${request.orderId}" }

            // Step 5 (Success): Build the success response DTO and send it.
            val successResponse = mapSessionToResponseDTO(session, request, correlationId)
            messageProducer.sendCheckoutSessionResponse(successResponse, correlationId)

        } catch (e: StripeException) {
            logger.error(e) { "Stripe API error during payment initiation for Order ID ${request.orderId}: ${e.message}" }
            // Step 5 (Stripe Failure): Build a specific error DTO and send it.
            val errorResponse = buildErrorResponse(request, correlationId, "Stripe API Error: ${e.message}")
            messageProducer.sendCheckoutSessionResponse(errorResponse, correlationId)
            // Optionally, re-throw a custom exception if you want the listener to reject the message.
            // For now, we handle it by sending a failure message.
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during payment initiation for Order ID ${request.orderId}" }
            // Step 5 (Unexpected Failure): Build a generic error DTO and send it.
            val errorResponse = buildErrorResponse(request, correlationId, "An unexpected internal error occurred.")
            messageProducer.sendCheckoutSessionResponse(errorResponse, correlationId)
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

        val response = PaymentInitiationResponse(
            orderId = orderId,
            customerEmail = session.customerEmail,
            sessionId = session.id,
            checkoutUrl = session.url ?: "", // URL might not be present on all session events
            paymentStatus = canonicalPaymentStatus,
            sessionStatus = canonicalSessionStatus,
            correlationId = session.id, // Use the session ID as the correlation for webhook events
            amountTotal = session.amountTotal,
            currency = session.currency,
            expiresAt = LocalDateTime.ofInstant(Instant.ofEpochSecond(session.expiresAt), ZoneOffset.UTC),
            metadata = session.metadata
        )

        // Step 5: Send the canonical DTO to the message queue.
        messageProducer.sendCheckoutSessionResponse(response, response.correlationId)
        logger.info { "Successfully processed and dispatched webhook event for Order ID: $orderId" }
    }


    /**
     * A private helper method that acts as the core of the Anti-Corruption Layer.
     * It translates a provider-specific Stripe Session object into the canonical,
     * provider-agnostic CheckoutSessionResponseDTO used throughout the system.
     *
     * @param session The Stripe Session object returned from the Stripe API.
     * @param request The original PaymentRequestDTO, used to retrieve the orderId.
     * @param correlationId The correlation ID for this transaction, for tracing.
     * @return A fully populated, canonical CheckoutSessionResponseDTO.
     */
    private fun mapSessionToResponseDTO(
        session: Session,
        request: PaymentInitiationRequest,
        correlationId: String?
    ): PaymentInitiationResponse {

        // --- Translation Logic for Payment Status ---
        // This `when` block translates Stripe's specific "payment_status" strings
        // into our system's universal CanonicalPaymentStatus.
        val canonicalPaymentStatus = when (session.paymentStatus) {
            "paid" -> CanonicalPaymentStatus.SUCCEEDED
            "unpaid" -> CanonicalPaymentStatus.PENDING
            "no_payment_required" -> CanonicalPaymentStatus.SUCCEEDED
            else -> {
                logger.warn { "Unknown Stripe payment_status '${session.paymentStatus}' for Session ID ${session.id}. Defaulting to UNKNOWN." }
                CanonicalPaymentStatus.UNKNOWN
            }
        }

        // --- Translation Logic for Session Status ---
        // This `when` block translates Stripe's specific "status" strings
        // into our system's universal CanonicalPaymentStatus.
        val canonicalSessionStatus = when (session.status) {
            "open" -> CanonicalPaymentStatus.PENDING
            "complete" -> CanonicalPaymentStatus.SUCCEEDED
            "expired" -> CanonicalPaymentStatus.EXPIRED
            else -> {
                logger.warn { "Unknown Stripe status '${session.status}' for Session ID ${session.id}. Defaulting to UNKNOWN." }
                CanonicalPaymentStatus.UNKNOWN
            }
        }

        // Construct the final, clean DTO using the translated values.
        return PaymentInitiationResponse(
            sessionId = session.id,
            orderId = request.orderId,
            checkoutUrl = session.url,

            // Use the canonical, translated statuses.
            paymentStatus = canonicalPaymentStatus,
            sessionStatus = canonicalSessionStatus,

            // Pass through critical information.
            correlationId = correlationId ?: "", // Ensure non-null for the DTO contract
            customerEmail = session.customerEmail ?: request.customerEmail, // Fallback to original request email

            // Pass through optional financial and metadata.
            amountTotal = session.amountTotal,
            currency = session.currency,
            expiresAt = LocalDateTime.ofInstant(Instant.ofEpochSecond(session.expiresAt), ZoneOffset.UTC),
            metadata = session.metadata
        )
    }

    private fun buildErrorResponse(
        request: PaymentInitiationRequest,
        correlationId: String?,
        errorMessage: String
    ): PaymentInitiationResponse {
        return PaymentInitiationResponse(
            sessionId = "N/A",
            orderId = request.orderId,
            sessionStatus = CanonicalPaymentStatus.UNKNOWN,
            paymentStatus = CanonicalPaymentStatus.FAILED,
            checkoutUrl = "", // No URL could be generated
            expiresAt = null,
            correlationId = correlationId ?: "",
            metadata = mapOf("error" to errorMessage),
            customerEmail = request.customerEmail,
            amountTotal = null,
            currency = null
        )
    }

    private fun buildStripeLineItemsFromRequest(request: PaymentInitiationRequest): List<SessionCreateParams.LineItem> {
        return request.lineItems.map { item ->
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