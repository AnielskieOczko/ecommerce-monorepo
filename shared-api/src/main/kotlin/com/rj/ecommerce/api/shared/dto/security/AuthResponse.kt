package com.rj.ecommerce.api.shared.dto.security

data class AuthResponse(
    val success: Boolean,
    val message: String?,
    val data: JwtResponse? = null
)