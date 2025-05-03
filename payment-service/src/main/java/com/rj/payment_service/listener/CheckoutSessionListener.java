package com.rj.payment_service.listener;

import com.rj.payment_service.dto.request.CheckoutSessionRequestDTO;
import com.rj.payment_service.dto.response.CheckoutSessionResponseDTO;
import com.rj.payment_service.producer.RabbitMQProducer;
import com.rj.payment_service.service.PaymentService;
import com.rj.payment_service.type.PaymentStatus;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.rj.payment_service.config.RabbitMQConfig.CHECKOUT_SESSION_QUEUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheckoutSessionListener {

    private final PaymentService paymentService;
    private final RabbitMQProducer rabbitMQProducer;

    @RabbitListener(queues = CHECKOUT_SESSION_QUEUE)
    public void handleCheckoutSessionRequest(
            CheckoutSessionRequestDTO request,
            Message message
    ) {
        String correlationId = message.getMessageProperties().getCorrelationId();
        log.info("Received checkout session request. CorrelationId: {}, OrderId: {}",
                correlationId, request.orderId());

        try {
            // Create the checkout session
            Session session = paymentService.createCheckoutSession(request);

            PaymentStatus sessionStatus = PaymentStatus.fromCheckoutSessionStatus(session.getStatus());
            PaymentStatus paymentStatus = PaymentStatus.fromCheckoutSessionPaymentStatus(session.getPaymentStatus());

            // Build the response
            CheckoutSessionResponseDTO response = CheckoutSessionResponseDTO.builder()
                    .sessionId(session.getId())
                    .orderId(request.orderId())
                    .sessionStatus(sessionStatus)
                    .paymentStatus(paymentStatus)
                    .checkoutUrl(session.getUrl())
                    .currency(session.getCurrency())
                    .amountTotal(session.getAmountTotal())
                    .customerEmail(session.getCustomerEmail())
                    .processedAt(LocalDateTime.now())
                    .build();

            // Log the response
            log.info("Checkout session response: {}", response);

            // Send the response back through RabbitMQ
            rabbitMQProducer.sendCheckoutSessionResponse(response, correlationId);

            log.info(
                    "Checkout session created successfully." + " CorrelationId: {}, SessionId: {}, OrderId: {}, url: {}",
                    correlationId, session.getId(), request.orderId(), session.getUrl());
        } catch (StripeException e) {
            log.error("Error creating checkout session for order: {}. CorrelationId: {}, Error: {}",
                    request.orderId(), correlationId, e.getMessage(), e);

            // Send error response
            CheckoutSessionResponseDTO errorResponse = CheckoutSessionResponseDTO.builder()
                    .orderId(request.orderId())
                    .sessionStatus(PaymentStatus.UNKNOWN)
                    .paymentStatus(PaymentStatus.UNKNOWN)
                    .processedAt(LocalDateTime.now())
                    .build();

            rabbitMQProducer.sendCheckoutSessionResponse(errorResponse, correlationId);
        } catch (Exception e) {
            log.error("Unexpected error creating checkout session for order: {}. CorrelationId: {}, Error: {}",
                    request.orderId(), correlationId, e.getMessage(), e);

            // Send error response for unexpected errors
            CheckoutSessionResponseDTO errorResponse = CheckoutSessionResponseDTO.builder()
                    .orderId(request.orderId())
                    .sessionStatus(PaymentStatus.UNKNOWN)
                    .processedAt(LocalDateTime.now())
                    .build();

            rabbitMQProducer.sendCheckoutSessionResponse(errorResponse, correlationId);
        }
    }
}
