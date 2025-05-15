package com.rj.ecommerce.api.shared.dto.security

import com.rj.ecommerce_backend.securityconfig.dto.JwtResponse

@Data
@Builder
class AuthResponse {
    private val success = false
    private val message: String? = null
    private val data: JwtResponse? = null
}
