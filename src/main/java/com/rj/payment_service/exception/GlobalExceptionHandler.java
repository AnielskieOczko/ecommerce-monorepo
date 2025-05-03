package com.rj.payment_service.exception;

import com.rj.payment_service.dto.stripe.ErrorResponse;
import com.stripe.exception.StripeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public ResponseEntity<ErrorResponse> handlePaymentProcessingException(PaymentProcessingException e) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("PAYMENT_PROCESSING_ERROR", e.getMessage()));
    }

    @ExceptionHandler(StripeException.class)
    public ResponseEntity<ErrorResponse> handleStripeException(StripeException e) {
        String errorCode = switch (e.getStatusCode()) {
            case 400 -> "INVALID_REQUEST_ERROR";
            case 401 -> "AUTHENTICATION_ERROR";
            case 402 -> "CARD_ERROR";
            case 403 -> "PERMISSION_ERROR";
            case 429 -> "RATE_LIMIT_ERROR";
            default -> "STRIPE_API_ERROR";
        };

        return ResponseEntity
                .status(e.getStatusCode() != null ? e.getStatusCode() : 500)
                .body(new ErrorResponse(errorCode, e.getMessage()));
    }

}
