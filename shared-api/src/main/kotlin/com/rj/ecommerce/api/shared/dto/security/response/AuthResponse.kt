package com.rj.ecommerce.api.shared.dto.security.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response containing authentication tokens and user details upon successful login or token refresh.")
data class AuthResponse(
    @field:Schema(description = "The JWT access token.", required = true)
    val token: String,

    @field:Schema(description = "The refresh token used to obtain a new access token.", required = true)
    val refreshToken: String,

    @field:Schema(description = "The unique ID of the authenticated user.", required = true)
    val id: Long,

    @field:Schema(description = "The email of the authenticated user.", required = true)
    val email: String,

    @field:Schema(description = "A list of roles/authorities assigned to the user.", required = true)
    val roles: List<String>,

    @field:Schema(description = "The type of token.", example = "Bearer", required = true)
    val type: String = "Bearer"
)