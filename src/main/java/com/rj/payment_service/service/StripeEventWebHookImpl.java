package com.rj.payment_service.service;

import com.rj.payment_service.dto.response.CheckoutSessionResponseDTO;
import com.rj.payment_service.producer.RabbitMQProducer;
import com.rj.payment_service.type.PaymentStatus;
import com.stripe.model.Charge;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeEventWebHookImpl implements StripeWebHook {

    private final RabbitMQProducer rabbitMQProducer;


    @Override
    public void handleChargeSucceeded(Charge charge) {
        log.info("Processing event: charge succeeded: {}", charge.getId());
        
        String orderId = charge.getMetadata().get("orderId");
        
        Map<String, String> additionalDetails = new HashMap<>();
        additionalDetails.put("receiptUrl", charge.getReceiptUrl());
        additionalDetails.put("paymentMethodType", charge.getPaymentMethodDetails().getType());

        PaymentStatus chargeStatus = PaymentStatus.fromChargeStatus(charge.getStatus());
        
        CheckoutSessionResponseDTO checkoutSessionResponseDTO = CheckoutSessionResponseDTO.builder()
                .orderId(orderId)
                // For charge events, we only care about payment status
                .paymentStatus(chargeStatus)
                .currency(charge.getCurrency())
                .amountTotal(charge.getAmount())
                .customerEmail(charge.getBillingDetails().getEmail())
                .processedAt(LocalDateTime.now())
                .additionalDetails(additionalDetails)
                .build();
        
        rabbitMQProducer.sendCheckoutSessionResponse(checkoutSessionResponseDTO, charge.getId());
        
        log.info("Charge succeeded for order: {}", orderId);
    }

    @Override
    public void handleCheckoutSessionCompleted(Session session) {
        log.info("Processing event: checkout session completed: {}", session.getId());

        String orderId = session.getMetadata().get("orderId");
        Map<String, String> additionalDetails = new HashMap<>();

        PaymentStatus sessionStatus = PaymentStatus.fromCheckoutSessionStatus(session.getStatus());
        PaymentStatus paymentStatus = PaymentStatus.fromCheckoutSessionPaymentStatus(session.getPaymentStatus());

        CheckoutSessionResponseDTO checkoutSessionResponseDTO = CheckoutSessionResponseDTO.builder()
                .sessionId(session.getId())
                .orderId(orderId)
                // For completed sessions, we always include both statuses
                .sessionStatus(sessionStatus)
                .paymentStatus(paymentStatus)
                .currency(session.getCurrency())
                .amountTotal(session.getAmountTotal())
                .customerEmail(session.getCustomerEmail())
                .processedAt(LocalDateTime.now())
                .additionalDetails(additionalDetails)
                .build();

        rabbitMQProducer.sendCheckoutSessionResponse(checkoutSessionResponseDTO, session.getId());

        log.info("Checkout session completed for order: {}", orderId);
    }

    @Override
    public void handleCheckoutSessionExpired(Session session) {
        log.info("Processing event: checkout session expired: {}", session.getId());

        String orderId = session.getMetadata().get("orderId");
        PaymentStatus sessionStatus = PaymentStatus.fromCheckoutSessionStatus(session.getStatus());
        PaymentStatus paymentStatus = PaymentStatus.fromCheckoutSessionPaymentStatus(session.getPaymentStatus());

        LocalDateTime expiresAt = session.getExpiresAt() != null ?
                LocalDateTime.ofEpochSecond(session.getExpiresAt(), 0, java.time.ZoneOffset.UTC) :
                null;

        Map<String, String> additionalDetails = new HashMap<>();

        CheckoutSessionResponseDTO checkoutSessionResponseDTO = CheckoutSessionResponseDTO.builder()
                .sessionId(session.getId())
                .orderId(orderId)
                // For expired sessions, we always include both statuses
                .sessionStatus(sessionStatus)
                .paymentStatus(paymentStatus)
                .currency(session.getCurrency())
                .amountTotal(session.getAmountTotal())
                .customerEmail(session.getCustomerEmail())
                .processedAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .additionalDetails(additionalDetails)
                .build();

        rabbitMQProducer.sendCheckoutSessionResponse(checkoutSessionResponseDTO, session.getId());

        log.warn("Checkout session expired for order: {}", orderId);
    }

    @Override
    public void handleUnknownEvent(String eventType, StripeObject object) {
        log.info("Unhandled event type: {} with object: {}", eventType, object);
        log.warn("Processed unknown Stripe event type: {}", eventType);
    }
}
