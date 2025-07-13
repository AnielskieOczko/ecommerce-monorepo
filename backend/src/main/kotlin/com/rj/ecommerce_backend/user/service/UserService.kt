package com.rj.ecommerce_backend.user.service

import com.rj.ecommerce.api.shared.dto.security.response.AuthResponse
import com.rj.ecommerce.api.shared.dto.user.request.ChangeAccountStatusRequest
import com.rj.ecommerce.api.shared.dto.user.request.ChangeEmailRequest
import com.rj.ecommerce.api.shared.dto.user.request.PasswordChangeRequest
import com.rj.ecommerce.api.shared.dto.user.request.UserUpdateDetailsRequest
import com.rj.ecommerce.api.shared.dto.user.response.UserResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

interface UserService {

    fun getProfile(userId: Long): UserResponse
    fun updateBasicDetails(userId: Long, request: UserUpdateDetailsRequest): UserResponse
    fun changeEmail(
        userId: Long,
        changeEmailRequest: ChangeEmailRequest,
        request: HttpServletRequest,
        response: HttpServletResponse): AuthResponse


    fun changePassword(userId: Long, request: PasswordChangeRequest)
    fun updateAccountStatus(userId: Long, request: ChangeAccountStatusRequest): UserResponse
    fun requestPasswordReset(email: String)
    fun resetPassword(token: String, newPassword: String)

    // Consider if this should also take HttpServletRequest/Response if it needs to invalidate sessions/cookies.
    fun deleteAccount(userId: Long)
}