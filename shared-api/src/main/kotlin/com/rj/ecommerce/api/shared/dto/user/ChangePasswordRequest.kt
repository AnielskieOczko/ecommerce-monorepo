package com.rj.ecommerce.api.shared.dto.user

/**
 * Request to change a user's password.
 *
 * @property currentPassword Current password for verification.
 * @property newPassword New password to set.
 *
 * Requirements:
 * - currentPassword and newPassword are required
 * - newPassword must meet security requirements
 * - newPassword must be different from currentPassword
 */
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)
