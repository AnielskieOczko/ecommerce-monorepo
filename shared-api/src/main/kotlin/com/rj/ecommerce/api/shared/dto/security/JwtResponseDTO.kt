package com.rj.ecommerce.api.shared.dto.security

data class JwtResponseDTO(
    val token: String,
    val refreshToken: String,
    val id: Long,
    val email: String,
    val roles: List<String>,
    val type: String = "Bearer"
)
