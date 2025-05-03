package com.rj.payment_service.controller;

import com.rj.payment_service.config.WebSecurityConfig;
import com.rj.payment_service.service.PaymentService;
import com.rj.payment_service.service.StripeWebHook;
import com.rj.payment_service.type.StripeEventType;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class StripeWebHookController {

    private final PaymentService paymentService;
    private final WebSecurityConfig stripeProperties;
    private final StripeWebHook eventHandler;

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signature
    ) throws SignatureVerificationException {
        log.info("=== Webhook Request Received ===");
        log.info("Signature header present: {}", signature != null);
        log.info("Webhook secret configured: {}", stripeProperties.getWebhookSecret() != null);
        log.debug("Payload: {}", payload);

        if (signature == null) {
            log.error("No Stripe signature found in request");
            return ResponseEntity.badRequest().build();
        }

        try {
            Event event = Webhook.constructEvent(
                    payload,
                    signature,
                    stripeProperties.getWebhookSecret()
            );
            
            log.info("Successfully constructed webhook event: {} with id: {}", 
                    event.getType(), event.getId());
            
            StripeObject stripeObject = event
                    .getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new RuntimeException("Failed to deserialize stripe object"));

            StripeEventType eventType = StripeEventType.fromStripeEventName(event.getType());

            if (eventType == null) {
                log.error("Unhandled event type: {}", event.getType());
                eventHandler.handleUnknownEvent(event.getType(), stripeObject);
                return ResponseEntity.ok().build();
            }

            switch (eventType) {
                case CHECKOUT_SESSION_COMPLETED -> eventHandler.handleCheckoutSessionCompleted((Session) stripeObject);
                case CHECKOUT_SESSION_EXPIRED -> eventHandler.handleCheckoutSessionExpired((Session) stripeObject);
                case CHARGE_SUCCEEDED -> eventHandler.handleChargeSucceeded((Charge) stripeObject);
            }
        } catch (SignatureVerificationException e) {
            log.error("Invalid signature for webhook request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().build();
    }

}
