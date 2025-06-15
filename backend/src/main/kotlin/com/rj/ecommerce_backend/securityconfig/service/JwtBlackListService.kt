package com.rj.ecommerce_backend.securityconfig.service

import com.rj.ecommerce.api.shared.dto.security.TokenInfo

interface JwtBlackListService {
    fun blackListToken(token: String, username: String)
    fun isTokenBlackListed(token: String): Boolean
    fun getUserTokens(userId: Long): List<TokenInfo>
    fun cleanupExpiredTokens(): Int
}