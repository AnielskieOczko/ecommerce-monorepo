package com.rj.ecommerce_backend.api.shared.dto.security.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Provides audit details about a specific blacklisted (logged out) token.")
data class TokenInfoResponse(
    @field:Schema(description = "The JWT that was blacklisted.", example = "eyJ...")
    val token: String,

    @field:Schema(description = "The timestamp when the token was blacklisted.")
    val blacklistedAt: LocalDateTime?,

    @field:Schema(description = "The original expiration timestamp of the token.")
    val expiresAt: LocalDateTime,

    @field:Schema(description = "The user or system agent that initiated the blacklisting (logout).", example = "user@example.com")
    val blacklistedBy: String?
)