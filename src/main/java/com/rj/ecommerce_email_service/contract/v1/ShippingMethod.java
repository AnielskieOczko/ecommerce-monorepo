package com.rj.ecommerce_email_service.contract.v1;

public enum ShippingMethod {
    INPOST,
    DHL;

    public static ShippingMethod fromString(String shippingMethod) {
        try {
            return ShippingMethod.valueOf(shippingMethod);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid shipping method: " + shippingMethod, e);
        }
    }
}
