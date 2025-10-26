package com.rj.ecommerce.api.shared.dto.security.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@Schema(description = "Request model for authenticating a user.")
data class LoginRequest(
    @field:Schema(description = "User's registered email address.", required = true, example = "user@example.com")
    @field:NotBlank(message = "Email must not be blank")
    @field:Email(message = "Email should be valid")
    val email: String,

    @field:Schema(description = "User's password.", required = true, example = "Str0ngP@ssw0rd!")
    @field:NotBlank(message = "Password must not be blank")
    val password: String
)