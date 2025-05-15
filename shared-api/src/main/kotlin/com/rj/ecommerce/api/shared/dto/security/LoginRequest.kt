package com.rj.ecommerce.api.shared.dto.security

import lombok.Data

@Data
class LoginRequest {
    private val email: @jakarta.validation.constraints.NotBlank kotlin.String? = null
    private val password: @jakarta.validation.constraints.NotBlank kotlin.String? = null
}
