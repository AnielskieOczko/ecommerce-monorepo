package com.rj.ecommerce.api.shared.dto.security

data class AuthResponseDTO(
    val success: Boolean,
    val message: String?,
    val data: JwtResponseDTO? = null
)