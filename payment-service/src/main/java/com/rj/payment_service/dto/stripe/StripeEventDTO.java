package com.rj.payment_service.dto.stripe;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record StripeEventDTO(
        String id,
        String type,
        Map<String, Object> data,
        LocalDateTime created
) {
}
