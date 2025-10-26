package com.rj.ecommerce_backend.user.service

import com.rj.ecommerce.api.shared.core.Email
import com.rj.ecommerce.api.shared.core.Password
import com.rj.ecommerce.api.shared.dto.security.response.AuthResponse
import com.rj.ecommerce.api.shared.dto.user.request.ChangeAccountStatusRequest
import com.rj.ecommerce.api.shared.dto.user.request.ChangeEmailRequest
import com.rj.ecommerce.api.shared.dto.user.request.PasswordChangeRequest
import com.rj.ecommerce.api.shared.dto.user.request.UserUpdateDetailsRequest
import com.rj.ecommerce.api.shared.dto.user.response.UserResponse
import com.rj.ecommerce_backend.security.SecurityContext
import com.rj.ecommerce_backend.security.repository.RefreshTokenRepository
import com.rj.ecommerce_backend.security.service.AuthenticationService
import com.rj.ecommerce_backend.user.domain.User
import com.rj.ecommerce_backend.user.exception.EmailAlreadyExistsException
import com.rj.ecommerce_backend.user.exception.InvalidCredentialsException
import com.rj.ecommerce_backend.user.exception.UserNotFoundException
import com.rj.ecommerce_backend.user.mapper.UserMapper
import com.rj.ecommerce_backend.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserServiceImpl(

    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userMapper: UserMapper,
    private val securityContext: SecurityContext,
    private val authService: AuthenticationService,
    private val refreshTokenRepository: RefreshTokenRepository
) : UserService {

    companion object {
        private val logger = KotlinLogging.logger { }
        private const val USER_NOT_FOUND_MSG_PREFIX = "User not found for id: "
    }

    @Transactional(readOnly = true)
    override fun getProfile(userId: Long): UserResponse {
        logger.debug { "Attempting to get profile data for user ID: $userId" }

        securityContext.ensureAccess(userId)
        logger.debug { "Access granted for user ID: $userId to view profile." }

        val user: User = findUserByIdOrThrow(userId)

        logger.info { "Successfully retrieved profile data for user ID: $userId" }
        return userMapper.toUserResponse(user)
    }

    @Transactional(readOnly = true)
    override fun updateBasicDetails(
        userId: Long,
        request: UserUpdateDetailsRequest
    ): UserResponse {
        securityContext.ensureAccess(userId)

        val user: User = findUserByIdOrThrow(userId)

        userMapper.updateUserFromBasicDetails(user, request)
        val savedUser: User = userRepository.save(user)

        return userMapper.toUserResponse(savedUser)
    }

    @Transactional
    override fun changeEmail(
        userId: Long,
        changeEmailRequest: ChangeEmailRequest,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): AuthResponse {
        logger.debug { "Processing email change request for user: $userId to new email: ${changeEmailRequest.newEmail}" }

        // 1. Authorization
        securityContext.ensureAccess(userId)
        logger.debug { "Access granted for user ID: $userId to change email." }

        val newEmailVO = Email(changeEmailRequest.newEmail)
        if (userRepository.existsByEmailAndIdNot(newEmailVO, userId)) {
            logger.warn { "Attempt to change email for user $userId to an already existing email: ${newEmailVO.value}" }
            throw EmailAlreadyExistsException("Email '${newEmailVO.value}' is already in use by another account.")
        }

        val user = findUserByIdOrThrow(userId)

        // 4. Verify current password (usually part of authService.handleEmailUpdate or done before)

            if (!passwordEncoder.matches(changeEmailRequest.currentPassword, user.password.value)) {
                throw InvalidCredentialsException("Current password does not match.")
            }

        val oldEmailValue = user.email.value
        user.email = newEmailVO

        // 6. Save the user with the new email
        userRepository.save(user)
        logger.info { "User entity email updated for ID ${user.id} from $oldEmailValue to ${user.email.value}. Proceeding with auth handling." }

        // 7. Handle authentication implications (e.g., invalidate old tokens, issue new ones)
        val authResponse = authService.handleEmailUpdate(
            user, // Pass the updated user object
            changeEmailRequest.currentPassword,
            request,
            response
        )

        logger.info { "Email change process completed for user ID: ${user.id}. Success: ${authResponse.type}" }
        return authResponse
    }

    @Transactional
    override fun changePassword(
        userId: Long,
        request: PasswordChangeRequest
    ) {
        securityContext.ensureAccess(userId)

        val user: User = findUserByIdOrThrow(userId)

        if (request.newPassword.isNotBlank()) {
            val encodedPassword = passwordEncoder.encode(request.newPassword)
            user.password = Password(encodedPassword)
        } else {
            logger.warn { "Attempt to set a blank password for user ${user.id}" }
            throw IllegalArgumentException("New password cannot be blank.")
        }

        userRepository.save(user)
        logger.info { "Successfully updated password for user id: ${user.id}" }
    }

    @Transactional
    override fun updateAccountStatus(
        userId: Long,
        request: ChangeAccountStatusRequest
    ): UserResponse {
        securityContext.ensureAccess(userId)

        val user: User = findUserByIdOrThrow(userId)

        val oldStatus = user.isActive
        user.isActive = request.isActive
        val savedUser: User = userRepository.save(user)
        logger.info { "Successfully updated account status for user id ${savedUser.id} from $oldStatus to ${savedUser.isActive}" }
        return userMapper.toUserResponse(savedUser)
    }

    override fun requestPasswordReset(email: String) {
        TODO("Not yet implemented")
    }

    override fun resetPassword(token: String, newPassword: String) {
        TODO("Not yet implemented")
    }

    @Transactional
    override fun deleteAccount(userId: Long) {
        securityContext.ensureAccess(userId)

        val user: User = findUserByIdOrThrow(userId)

        val userIdToDelete = user.id ?: throw IllegalStateException("Cannot delete user without an ID.")

        refreshTokenRepository.deleteByUserId(userIdToDelete)
        userRepository.delete(user)
    }

    /**
     * Private helper to find a user by ID or throw UserNotFoundException.
     * Also logs a warning if not found, including the current authenticated user for context.
     */
    private fun findUserByIdOrThrow(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow {
                // It's good to know WHO tried to access a non-existent user after passing initial security checks
                val currentAuthUser = try {
                    securityContext.getCurrentUser().id
                } catch (e: Exception) {
                    "UNKNOWN_OR_ANONYMOUS"
                }
                logger.warn { "$USER_NOT_FOUND_MSG_PREFIX$userId (operation initiated by authenticated user: $currentAuthUser)" }
                UserNotFoundException("$USER_NOT_FOUND_MSG_PREFIX$userId")
            }
    }


}