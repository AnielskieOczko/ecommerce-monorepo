package com.rj.ecommerce_backend.user.service

import com.rj.ecommerce.api.shared.dto.security.AuthResponseDTO
import com.rj.ecommerce.api.shared.dto.user.ChangeAccountStatusDTO
import com.rj.ecommerce.api.shared.dto.user.ChangeEmailRequestDTO
import com.rj.ecommerce.api.shared.dto.user.ChangePasswordRequestDTO
import com.rj.ecommerce.api.shared.dto.user.UpdateBasicDetailsRequestDTO
import com.rj.ecommerce.api.shared.dto.user.UserInfoDTO
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

interface UserService {

    fun getProfile(userId: Long): UserInfoDTO
    fun updateBasicDetails(userId: Long, request: UpdateBasicDetailsRequestDTO): UserInfoDTO
    fun changeEmail(
        userId: Long,
        changeEmailRequest: ChangeEmailRequestDTO,
        request: HttpServletRequest,
        response: HttpServletResponse): AuthResponseDTO


    fun changePassword(userId: Long, request: ChangePasswordRequestDTO)
    fun updateAccountStatus(userId: Long, request: ChangeAccountStatusDTO): UserInfoDTO
    fun requestPasswordReset(email: String)
    fun resetPassword(token: String, newPassword: String)

    // Consider if this should also take HttpServletRequest/Response if it needs to invalidate sessions/cookies.
    fun deleteAccount(userId: Long)
}