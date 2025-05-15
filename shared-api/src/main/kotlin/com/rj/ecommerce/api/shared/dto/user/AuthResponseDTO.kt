package com.rj.ecommerce.api.shared.dto.user

/**
 * Response containing authentication tokens.
 *
 * @property token JWT access token.
 * @property refreshToken JWT refresh token.
 * @property userId ID of the authenticated user.
 * @property email Email of the authenticated user.
 * @property roles List of roles assigned to the authenticated user.
 *
 * Requirements:
 * - All fields are required
 */
data class AuthResponseDTO(
    val token: String,
    val refreshToken: String,
    val userId: Long,
    val email: String,
    val roles: List<String>
)
