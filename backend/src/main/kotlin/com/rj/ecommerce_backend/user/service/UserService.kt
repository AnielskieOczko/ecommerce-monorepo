package com.rj.ecommerce_backend.user.service

import com.rj.ecommerce.api.shared.dto.security.AuthResponse
import com.rj.ecommerce.api.shared.dto.user.AccountStatusRequestDTO
import com.rj.ecommerce.api.shared.dto.user.ChangeEmailRequest
import com.rj.ecommerce.api.shared.dto.user.ChangePasswordRequestDTO
import com.rj.ecommerce.api.shared.dto.user.UpdateBasicDetailsRequest
import com.rj.ecommerce.api.shared.dto.user.UserInfoDTO
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

interface UserService {

    fun getProfile(userId: Long): UserInfoDTO
    fun updateBasicDetails(userId: Long, request: UpdateBasicDetailsRequest): UserInfoDTO
    fun changeEmail(
        userId: Long,
        changeEmailRequest: ChangeEmailRequest,
        request: HttpServletRequest,
        response: HttpServletResponse): AuthResponse


    fun changePassword(userId: Long, request: ChangePasswordRequestDTO)
    fun updateAccountStatus(userId: Long, request: AccountStatusRequestDTO): UserInfoDTO
    fun requestPasswordReset(email: String)
    fun resetPassword(token: String, newPassword: String)

    // Consider if this should also take HttpServletRequest/Response if it needs to invalidate sessions/cookies.
    fun deleteAccount(userId: Long)
}