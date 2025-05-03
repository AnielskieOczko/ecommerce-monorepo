package com.rj.ecommerce_email_service.contract.v1;

public enum EmailTemplate {
    ORDER_CONFIRMATION("order-confirmation"),
    ORDER_SHIPMENT("order-shipment"),
    ORDER_CANCELLED("order-cancelled"),
    ORDER_REFUNDED("order-refunded"),
    CUSTOMER_WELCOME("customer-welcome"),

    // Payment templates
    PAYMENT_CONFIRMATION("payment-confirmation"),
    PAYMENT_FAILED("payment-failed"),
    PAYMENT_ERROR_ADMIN("payment-error-admin"),
    PAYMENT_ERROR_CUSTOMER("payment-error-customer"),

    TEST_MESSAGE("test-message-template");

    private final String templateId;

    EmailTemplate(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public static EmailTemplate fromTemplateId(String templateId) {
        for (EmailTemplate template : EmailTemplate.values()) {
            if (template.getTemplateId().equals(templateId)) {
                return template;
            }
        }
        throw new IllegalArgumentException("Invalid templateId: " + templateId);
    }
}
