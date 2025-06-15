package com.rj.ecommerce.api.shared.dto.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ChangeEmailRequestDTO(
    @field:NotBlank
    val currentPassword: String,
    @field:NotBlank @field:Email
    val newEmail: String
)
