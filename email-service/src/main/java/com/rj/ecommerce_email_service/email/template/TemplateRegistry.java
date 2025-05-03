package com.rj.ecommerce_email_service.email.template;

import com.rj.ecommerce_email_service.contract.v1.EmailTemplate;
import org.springframework.stereotype.Component;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.EnumMap;
import java.util.Map;

/**
 * Registry for email templates.
 * Maps EmailTemplate enum values to actual template paths.
 */
@Component
public class TemplateRegistry {
    
    private final SpringTemplateEngine templateEngine;
    private final Map<EmailTemplate, String> templatePaths = new EnumMap<>(EmailTemplate.class);
    
    public TemplateRegistry(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        
        // Register default templates

        // Order templates
        templatePaths.put(EmailTemplate.ORDER_CONFIRMATION, "order-confirmation");
        templatePaths.put(EmailTemplate.ORDER_SHIPMENT, "order-shipment");
        templatePaths.put(EmailTemplate.ORDER_CANCELLED, "order-cancelled");
        templatePaths.put(EmailTemplate.ORDER_REFUNDED, "order-refunded");
        templatePaths.put(EmailTemplate.CUSTOMER_WELCOME, "customer-welcome");
        templatePaths.put(EmailTemplate.TEST_MESSAGE, "test-message-template");

        // Payment templates
        templatePaths.put(EmailTemplate.PAYMENT_CONFIRMATION, "payment-confirmation");
        templatePaths.put(EmailTemplate.PAYMENT_FAILED, "payment-failed");
        templatePaths.put(EmailTemplate.PAYMENT_ERROR_ADMIN, "payment-error-admin");
        templatePaths.put(EmailTemplate.PAYMENT_ERROR_CUSTOMER, "payment-error-customer");
    }
    
    /**
     * Get the template path for a given email template
     */
    public String getTemplatePath(EmailTemplate template) {
        String path = templatePaths.get(template);
        if (path == null) {
            throw new IllegalArgumentException("No template path registered for: " + template);
        }
        return path;
    }
    
    /**
     * Register a new template or override an existing one
     */
    public void registerTemplate(EmailTemplate template, String templatePath) {
        templatePaths.put(template, templatePath);
    }
}
