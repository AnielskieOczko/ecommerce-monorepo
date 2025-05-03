package com.rj.payment_service.dto.stripe;

import lombok.Builder;

@Builder
public record ErrorResponse(
        String code,
        String message
) {
}
