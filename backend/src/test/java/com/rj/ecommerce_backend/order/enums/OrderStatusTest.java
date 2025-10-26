package com.rj.ecommerce_backend.order.enums;

import com.rj.ecommerce.api.shared.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTest {

    @Test
    void shouldConvertValidStringToOrderStatus() {
        // Given & When & Then
        assertEquals(com.rj.ecommerce.api.shared.enums.OrderStatus.PENDING, com.rj.ecommerce.api.shared.enums.OrderStatus.fromString("PENDING"));
        assertEquals(com.rj.ecommerce.api.shared.enums.OrderStatus.CONFIRMED, com.rj.ecommerce.api.shared.enums.OrderStatus.fromString("CONFIRMED"));
        assertEquals(com.rj.ecommerce.api.shared.enums.OrderStatus.PROCESSING, com.rj.ecommerce.api.shared.enums.OrderStatus.fromString("PROCESSING"));
        assertEquals(com.rj.ecommerce.api.shared.enums.OrderStatus.SHIPPED, com.rj.ecommerce.api.shared.enums.OrderStatus.fromString("SHIPPED"));
        assertEquals(com.rj.ecommerce.api.shared.enums.OrderStatus.DELIVERED, com.rj.ecommerce.api.shared.enums.OrderStatus.fromString("DELIVERED"));
        assertEquals(com.rj.ecommerce.api.shared.enums.OrderStatus.CANCELLED, com.rj.ecommerce.api.shared.enums.OrderStatus.fromString("CANCELLED"));
        assertEquals(com.rj.ecommerce.api.shared.enums.OrderStatus.REFUNDED, com.rj.ecommerce.api.shared.enums.OrderStatus.fromString("REFUNDED"));
        assertEquals(com.rj.ecommerce.api.shared.enums.OrderStatus.FAILED, com.rj.ecommerce.api.shared.enums.OrderStatus.fromString("FAILED"));
    }

    @Test
    void shouldConvertAllEnumValuesToString() {
        // Test all enum values can be converted to string and back
        for (com.rj.ecommerce.api.shared.enums.OrderStatus status : com.rj.ecommerce.api.shared.enums.OrderStatus.values()) {
            assertEquals(status, com.rj.ecommerce.api.shared.enums.OrderStatus.fromString(status.name()));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVALID", "pending", "Confirmed", "", "UNKNOWN"})
    void shouldThrowExceptionForInvalidOrderStatus(String invalidStatus) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> OrderStatus.fromString(invalidStatus)
        );
        
        assertTrue(exception.getMessage().contains("Invalid order status"));
    }
}
