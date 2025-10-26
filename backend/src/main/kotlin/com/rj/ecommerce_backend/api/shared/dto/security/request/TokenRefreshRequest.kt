package com.rj.ecommerce_backend.api.shared.dto.security.request

import jakarta.validation.constraints.NotBlank

data class TokenRefreshRequest(
    @field:NotBlank(message = "Refresh token must not be blank")
    val refreshToken: String
)