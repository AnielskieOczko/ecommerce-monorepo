package com.rj.ecommerce.api.shared.dto.security

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "Email must not be blank")
    @field:Email(message = "Email should be valid")
    val email: String,
    @field:NotBlank(message = "Password must not be blank")
    val password: String
)