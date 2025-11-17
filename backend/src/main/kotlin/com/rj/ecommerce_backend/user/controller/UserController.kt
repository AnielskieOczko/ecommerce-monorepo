package com.rj.ecommerce_backend.user.controller

import com.rj.ecommerce_backend.api.shared.dto.security.response.AuthResponse
import com.rj.ecommerce_backend.api.shared.dto.user.request.ChangeEmailRequest
import com.rj.ecommerce_backend.api.shared.dto.user.request.PasswordChangeRequest
import com.rj.ecommerce_backend.api.shared.dto.user.request.UserUpdateDetailsRequest
import com.rj.ecommerce_backend.api.shared.dto.user.response.UserResponse
import com.rj.ecommerce_backend.user.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {

    companion object {
        private val logger = KotlinLogging.logger { }
    }

    @GetMapping("/{userId}/profile")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    fun getUserProfile(@PathVariable userId: Long): ResponseEntity<UserResponse> {
        logger.info { "Request to get profile for user ID: $userId" }
        return ResponseEntity.ok(userService.getProfile(userId))
    }

    @PutMapping("/{userId}/email")
    @PreAuthorize("#userId == authentication.principal.id")
    fun updateUserEmail(
        @PathVariable userId: Long,
        @Valid @RequestBody changeEmailRequest: ChangeEmailRequest,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<AuthResponse> {
        logger.info { "Request to update email for user ID: $userId" }
        val authResponse: AuthResponse = userService.changeEmail(
            userId, changeEmailRequest, request, response
        )

        return ResponseEntity.ok(authResponse)
    }

    @PutMapping("/{userId}/password")
    @PreAuthorize("#userId == authentication.principal.id")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateUserPassword(
        @PathVariable userId: Long,
        @Valid @RequestBody changePasswordRequestDTO: PasswordChangeRequest
    ) {
        logger.info { "Request to update password for user ID: $userId" }
        userService.changePassword(userId, changePasswordRequestDTO)
    }

    @PutMapping("/{userId}/details")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    fun updateUserBasicDetails(
        @PathVariable userId: Long,
        @Valid @RequestBody updateBasicDetailsRequest: UserUpdateDetailsRequest
    ): UserResponse {
        logger.info { "Request to update basic details for user ID: $userId" }
        return userService.updateBasicDetails(userId, updateBasicDetailsRequest)
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUserAccount(@PathVariable userId: Long) {
        logger.info { "Request to delete account for user ID: $userId" }
        userService.deleteAccount(userId)
    }

    // --- Missing Password Reset Endpoints (from UserService interface) ---
    @PostMapping("/password-reset/request")
    @ResponseStatus(HttpStatus.ACCEPTED) // 202 Accepted: request received, processing will happen
    fun requestPasswordReset(@RequestParam email: String) {
        logger.info { "Request for password reset for email: $email" }
        userService.requestPasswordReset(email)
    }

    @PostMapping("/password-reset/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun resetPassword(
        @RequestParam token: String,
        // Typically new password comes in request body for security, not query param
        @Valid @RequestBody newPasswordRequest: PasswordChangeRequest
    ) {
        logger.info { "Attempting to reset password with token." }
        userService.resetPassword(token, newPasswordRequest.newPassword)
    }


}