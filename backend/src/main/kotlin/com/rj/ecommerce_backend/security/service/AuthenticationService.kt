package com.rj.ecommerce_backend.security.service

import com.rj.ecommerce.api.shared.dto.security.AuthResponseDTO
import com.rj.ecommerce.api.shared.dto.security.LoginRequestDTO
import com.rj.ecommerce.api.shared.dto.security.TokenRefreshRequestDTO
import com.rj.ecommerce_backend.user.domain.User
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

interface AuthenticationService {
    fun authenticateUser(loginRequest: LoginRequestDTO): AuthResponseDTO
    fun refreshToken(tokenRefreshRequest: TokenRefreshRequestDTO): AuthResponseDTO
    fun handleEmailUpdate(
        user: User,
        currentPassword: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): AuthResponseDTO
}