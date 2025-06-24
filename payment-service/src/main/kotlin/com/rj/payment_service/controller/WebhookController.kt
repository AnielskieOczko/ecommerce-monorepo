package com.rj.payment_service.controller

import com.rj.payment_service.config.StripeProperties
import com.rj.payment_service.service.PaymentService
import com.rj.payment_service.service.StripeWebhook
import com.rj.payment_service.type.StripeEventType
import com.stripe.exception.SignatureVerificationException
import com.stripe.model.Charge
import com.stripe.model.Event
import com.stripe.model.StripeObject
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/payments")
class StripeWebhookController(
    private val paymentService: PaymentService,
    private val stripeProperties: StripeProperties,
    private val eventHandler: StripeWebhook
) {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    @PostMapping("/webhook")
    fun handleStripeWebhook(
        @RequestBody payload: String,
        @RequestHeader(value = "Stripe-signature", required = false) signature: String?
    ) {
        if (signature.isNullOrBlank()) {
            return ResponseEntity.badRequest().build()
        }

        try {
            val event: Event = Webhook.constructEvent(
                payload,
                signature,
                stripeProperties.webhookSecret
            )

            val stripeObject: StripeObject = event
                .dataObjectDeserializer
                .`object`
                .orElseThrow { RuntimeException("") }

            val eventType: StripeEventType? = StripeEventType.valueOf(event.type)

            if (eventType == null) {
                eventHandler.handleUnknownEvent(event.type, stripeObject)
                return ResponseEntity.ok().build()
            }
            when (eventType) {
                StripeEventType.CHECKOUT_SESSION_COMPLETED -> eventHandler.handleCheckoutSessionCompleted(stripeObject as Session?)
                StripeEventType.CHECKOUT_SESSION_EXPIRED -> eventHandler.handleCheckoutSessionExpired(stripeObject as Session?)
                StripeEventType.CHARGE_SUCCEEDED -> eventHandler.handleChargeSucceeded(stripeObject as Charge?)
            }
        } catch (e: SignatureVerificationException) {
            return ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}