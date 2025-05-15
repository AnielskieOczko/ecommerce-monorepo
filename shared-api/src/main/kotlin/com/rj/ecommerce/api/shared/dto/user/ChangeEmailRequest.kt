package com.rj.ecommerce.api.shared.dto.user

import jakarta.validation.constraints.NotNull

@JvmRecord
data class ChangeEmailRequest(
    val currentPassword: @NotNull String?,
    val newEmail: @NotNull String?
)
