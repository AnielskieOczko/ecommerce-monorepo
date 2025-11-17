package com.rj.ecommerce_backend.security.service

import com.rj.ecommerce_backend.api.shared.dto.security.response.TokenInfoResponse

interface JwtBlackListService {
    fun blackListToken(token: String, username: String)
    fun isTokenBlackListed(token: String): Boolean
    fun getUserTokens(userId: Long): List<TokenInfoResponse>
    fun cleanupExpiredTokens(): Int
}