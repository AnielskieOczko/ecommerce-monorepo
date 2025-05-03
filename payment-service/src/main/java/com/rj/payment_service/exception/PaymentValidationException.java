package com.rj.payment_service.exception;

public class PaymentValidationException extends RuntimeException {
    public PaymentValidationException(String message, Throwable cause) {
        super(message);
    }
}
