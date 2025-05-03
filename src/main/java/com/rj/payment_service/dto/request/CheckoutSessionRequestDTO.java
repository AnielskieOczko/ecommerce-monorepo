package com.rj.payment_service.dto.request;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record CheckoutSessionRequestDTO(
        String orderId,
        String customerEmail,
        String successUrl,
        String cancelUrl,
        List<CheckoutLineItemDTO> lineItems,
        Map<String, String> metadata
) {
    @Builder
    public record CheckoutLineItemDTO(
            String name,
            String description,
            Long unitAmount,
            Integer quantity,
            String currency
    ) {
    }
}
