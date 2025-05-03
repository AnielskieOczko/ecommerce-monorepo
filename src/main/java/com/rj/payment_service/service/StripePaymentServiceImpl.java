package com.rj.payment_service.service;

import com.rj.payment_service.dto.request.CheckoutSessionRequestDTO;
import com.rj.payment_service.exception.PaymentProcessingException;
import com.rj.payment_service.type.Currency;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentServiceImpl implements PaymentService {


    @Override
    public Session createCheckoutSession(CheckoutSessionRequestDTO request) throws StripeException {
        log.info("Creating checkout session for order: {}", request.orderId());

        Long thirtyMinutesFromNow = LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC) + (30 * 60);

        try {
            // Validate and prepare the request
            validateRequest(request);

            // Prepare line items
            List<SessionCreateParams.LineItem> lineItems = buildStripeLineItemsFromRequest(request);

            // Build the session parameters
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setCustomerEmail(request.customerEmail())
                    .setSuccessUrl(request.successUrl())
                    .setCancelUrl(request.cancelUrl())
                    .addAllLineItem(lineItems)
                    .setExpiresAt(thirtyMinutesFromNow)
                    .setPaymentIntentData(
                            SessionCreateParams.PaymentIntentData.builder()
                                    .putMetadata("orderId", request.orderId())
                                    .build()
                    )
                    .putMetadata("orderId", request.orderId());

            // Add any additional metadata
            if (request.metadata() != null) {
                request.metadata().forEach(paramsBuilder::putMetadata);
            }

//            Create the session
//            Makes the API call to Stripe: Session.create(paramsBuilder.build())
//            This is where the actual communication with Stripe happens
//            Returns a Session object with ID, URL, and other details
            Session session = Session.create(paramsBuilder.build());
            log.info("Checkout URL: {}", session.getUrl());
            log.info("Created checkout session: {} for order: {}", session.getId(), request.orderId());
            return session;
        } catch (StripeException e) {
            log.error("Error creating checkout session", e);
            throw new PaymentProcessingException("Failed to create checkout session", e);
        }
    }

    private List<SessionCreateParams.LineItem> buildStripeLineItemsFromRequest(CheckoutSessionRequestDTO request) {
        // Create line items for the session
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

        for (CheckoutSessionRequestDTO.CheckoutLineItemDTO item : request.lineItems()) {
            lineItems.add(
                    SessionCreateParams.LineItem.builder()
                            .setQuantity(Long.valueOf(item.quantity()))
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency(item.currency().toLowerCase())
                                            .setUnitAmount(convertToSmallestCurrencyUnit(item.unitAmount(), item.currency()))
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName(item.name())
                                                            .setDescription(item.description())
                                                            .build()
                                            )
                                            .build()
                            )
                            .build()
            );
        }
        return lineItems;
    }

    private boolean isValidEmail(String email) {
        // Basic email validation - you might want to use a more robust solution
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private Long convertToSmallestCurrencyUnit(Long amount, String currency) {
        Currency currencyEnum = Currency.fromCode(currency);
        return switch (currencyEnum) {
            case USD, EUR, PLN -> amount * 100;
        };
    }

    private void validateRequest(CheckoutSessionRequestDTO request) {
        // Validate request
        if (request.orderId() == null || request.orderId().trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID is required");
        }
        if (request.customerEmail() == null || !isValidEmail(request.customerEmail())) {
            throw new IllegalArgumentException("Valid customer email is required");
        }
        if (request.successUrl() == null || request.successUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Success URL is required");
        }
        if (request.cancelUrl() == null || request.cancelUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Cancel URL is required");
        }
        if (request.lineItems() == null || request.lineItems().isEmpty()) {
            throw new IllegalArgumentException("At least one line item is required");
        }
    }
}
