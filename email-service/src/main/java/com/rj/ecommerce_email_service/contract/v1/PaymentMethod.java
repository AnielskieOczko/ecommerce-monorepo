package com.rj.ecommerce_email_service.contract.v1;

public enum PaymentMethod {
    CREDIT_CARD,
    PAYPAL,
    BANK_TRANSFER,
    BLIK;

    public static PaymentMethod fromString(String paymentMethod) {
        try {
            return PaymentMethod.valueOf(paymentMethod);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment method: " + paymentMethod, e);
        }
    }
}
