package com.rj.ecommerce_backend.security.service

import com.rj.ecommerce_backend.security.domain.RefreshToken

interface RefreshTokenService {
    fun createRefreshToken(userId: Long): RefreshToken
    fun verifyRefreshToken(token: String): RefreshToken

}