package com.rj.ecommerce_backend.securityconfig.service

import com.rj.ecommerce_backend.securityconfig.domain.RefreshToken

interface RefreshTokenService {
    fun createRefreshToken(userId: Long): RefreshToken
    fun verifyRefreshToken(token: String): RefreshToken

}