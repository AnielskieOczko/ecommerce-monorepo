package com.rj.ecommerce_backend.api.shared.dto.user.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "A sealed interface representing different ways a password can be changed.")
sealed interface PasswordChangeRequest {
    @get:NotBlank
    @get:Size(min = 8, message = "Password must be at least 8 characters long")
    val newPassword: String
}

@Schema(description = "Request for an authenticated user to change their own password.")
data class AuthenticatedPasswordChange(
    @field:Schema(description = "The user's current password for verification.", required = true)
    @field:NotBlank
    val currentPassword: String,

    @field:Schema(description = "The desired new password.", required = true)
    override val newPassword: String
) : PasswordChangeRequest

@Schema(description = "Request to reset a password using a verification token.")
data class TokenBasedPasswordReset(
    @field:Schema(description = "The password reset token sent to the user.", required = true)
    @field:NotBlank
    val resetToken: String,

    @field:Schema(description = "The desired new password.", required = true)
    override val newPassword: String
) : PasswordChangeRequest
