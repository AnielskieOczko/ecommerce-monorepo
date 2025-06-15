package com.rj.ecommerce_backend.securityconfig.service

import com.rj.ecommerce.api.shared.dto.security.AuthResponse
import com.rj.ecommerce.api.shared.dto.security.LoginRequest
import com.rj.ecommerce.api.shared.dto.security.TokenRefreshRequest
import com.rj.ecommerce_backend.user.domain.User
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

interface AuthenticationService {
    fun authenticateUser(loginRequest: LoginRequest): AuthResponse
    fun refreshToken(tokenRefreshRequest: TokenRefreshRequest): AuthResponse
    fun handleEmailUpdate(
        user: User,
        currentPassword: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): AuthResponse
}