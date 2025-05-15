package com.rj.ecommerce_backend.messaging.email.contract.v1;

import com.rj.ecommerce.api.shared.enums.EmailTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EmailTemplateTest {

    @Test
    void shouldReturnCorrectTemplateId() {
        // Given & When & Then
        assertEquals("customer-welcome", com.rj.ecommerce.api.shared.enums.EmailTemplate.CUSTOMER_WELCOME.getTemplateId());
        assertEquals("order-confirmation", com.rj.ecommerce.api.shared.enums.EmailTemplate.ORDER_CONFIRMATION.getTemplateId());
        assertEquals("payment-failed", com.rj.ecommerce.api.shared.enums.EmailTemplate.PAYMENT_FAILED.getTemplateId());
        assertEquals("test-message-template", com.rj.ecommerce.api.shared.enums.EmailTemplate.TEST_MESSAGE.getTemplateId());
    }

    @Test
    void shouldFindTemplateByTemplateId() {
        // Given
        String templateId = "customer-welcome";

        // When
        com.rj.ecommerce.api.shared.enums.EmailTemplate template = com.rj.ecommerce.api.shared.enums.EmailTemplate.fromTemplateId(templateId);

        // Then
        assertEquals(com.rj.ecommerce.api.shared.enums.EmailTemplate.CUSTOMER_WELCOME, template);
    }

    @Test
    void shouldFindAllTemplatesByTheirIds() {
        // Test all enum values can be found by their IDs
        for (com.rj.ecommerce.api.shared.enums.EmailTemplate template : com.rj.ecommerce.api.shared.enums.EmailTemplate.values()) {
            assertEquals(template, com.rj.ecommerce.api.shared.enums.EmailTemplate.fromTemplateId(template.getTemplateId()));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-template", "", "unknown", "welcome"})
    void shouldThrowExceptionForInvalidTemplateId(String invalidTemplateId) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> EmailTemplate.fromTemplateId(invalidTemplateId)
        );
        
        assertEquals("Invalid templateId: " + invalidTemplateId, exception.getMessage());
    }
}
