package com.rj.ecommerce.api.shared.dto.user

import jakarta.validation.constraints.NotBlank

data class ResetPasswordRequestDTO(
    @field:NotBlank
    val newPassword: String) {
}