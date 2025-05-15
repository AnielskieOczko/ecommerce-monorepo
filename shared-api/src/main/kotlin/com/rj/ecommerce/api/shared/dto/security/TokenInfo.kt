package com.rj.ecommerce.api.shared.dto.security

import java.time.LocalDateTime

data class TokenInfo(
    val token: String,
    val blacklistedAt: LocalDateTime? = null,
    val expiresAt: LocalDateTime,
    val blacklistedBy: String? = null
)
