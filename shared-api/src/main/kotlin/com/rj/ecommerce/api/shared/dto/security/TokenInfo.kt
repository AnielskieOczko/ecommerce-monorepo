package com.rj.ecommerce.api.shared.dto.security

import lombok.AllArgsConstructor

@Data
@AllArgsConstructor
class TokenInfo {
    private val token: String? = null
    private val blacklistedAt: LocalDateTime? = null
    private val expiresAt: LocalDateTime? = null
    private val blacklistedBy: String? = null
}
