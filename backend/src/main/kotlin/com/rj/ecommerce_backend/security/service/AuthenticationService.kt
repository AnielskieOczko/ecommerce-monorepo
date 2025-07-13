package com.rj.ecommerce_backend.security.service

import com.rj.ecommerce.api.shared.dto.security.request.LoginRequest
import com.rj.ecommerce.api.shared.dto.security.request.TokenRefreshRequest
import com.rj.ecommerce.api.shared.dto.security.response.AuthResponse
import com.rj.ecommerce_backend.user.domain.User
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

interface AuthenticationService {

    /**
     * Authenticates a user with the given credentials.
     * @return An [AuthResponse] containing new tokens on success.
     * @throws com.rj.ecommerce_backend.security.exception.UserAuthenticationException if authentication fails.
     */
    fun authenticateUser(loginRequest: LoginRequest): AuthResponse

    /**
     * Refreshes an access token using a valid refresh token.
     * @return A new [AuthResponse] containing new tokens on success.
     * @throws com.rj.ecommerce_backend.security.exception.TokenRefreshException if the refresh token is invalid or expired.
     */
    fun refreshToken(tokenRefreshRequest: TokenRefreshRequest): AuthResponse

    /**
     * Handles the complex flow of updating a user's email, which requires re-authentication.
     * @return A new [AuthResponse] containing new tokens for the session with the updated email.
     * @throws com.rj.ecommerce_backend.security.exception.UserAuthenticationException if re-authentication fails.
     */
    fun handleEmailUpdate(
        user: User,
        currentPassword: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): AuthResponse
}