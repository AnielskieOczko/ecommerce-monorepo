package com.rj.ecommerce_backend.api.shared.dto.user.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ChangeEmailRequest(
    @field:NotBlank
    val currentPassword: String,
    @field:NotBlank @field:Email
    val newEmail: String
)
