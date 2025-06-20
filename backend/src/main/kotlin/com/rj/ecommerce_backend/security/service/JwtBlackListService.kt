package com.rj.ecommerce_backend.security.service

import com.rj.ecommerce.api.shared.dto.security.TokenInfoDTO

interface JwtBlackListService {
    fun blackListToken(token: String, username: String)
    fun isTokenBlackListed(token: String): Boolean
    fun getUserTokens(userId: Long): List<TokenInfoDTO>
    fun cleanupExpiredTokens(): Int
}