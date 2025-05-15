package com.rj.ecommerce.api.shared.dto.security

import lombok.Data

@Data
class JwtResponse(
    private val token: String?,
    private val refreshToken: String?,
    private val id: Long?,
    private val email: String?,
    private val roles: MutableList<String?>?
) {
    private val type = "Bearer"
}
