package com.rj.ecommerce.api.shared.dto.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ChangeEmailRequest(
    @field:NotBlank
    val currentPassword: String,
    @field:NotBlank @field:Email
    val newEmail: String
)
