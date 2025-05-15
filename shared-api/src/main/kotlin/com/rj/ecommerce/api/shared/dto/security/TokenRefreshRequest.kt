package com.rj.ecommerce.api.shared.dto.security

import lombok.Data

@Data
class TokenRefreshRequest {
    private val refreshToken: @jakarta.validation.constraints.NotBlank kotlin.String? = null
}
