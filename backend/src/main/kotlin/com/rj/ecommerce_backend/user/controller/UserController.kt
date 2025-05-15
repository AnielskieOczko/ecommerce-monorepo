package com.rj.ecommerce_backend.user.controller

import com.rj.ecommerce.api.shared.dto.security.AuthResponse
import com.rj.ecommerce.api.shared.dto.user.AccountStatusRequestDTO
import com.rj.ecommerce.api.shared.dto.user.ChangeEmailRequest
import com.rj.ecommerce.api.shared.dto.user.ChangePasswordRequestDTO
import com.rj.ecommerce.api.shared.dto.user.UpdateBasicDetailsRequest
import com.rj.ecommerce.api.shared.dto.user.UserInfoDTO
import com.rj.ecommerce_backend.user.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserControllerImpl(
    private val userService: UserService
) {

    companion object {
        private val logger = KotlinLogging.logger { }
    }

    @GetMapping("/{userId}/profile")
    fun getUserProfile(@PathVariable userId: Long): ResponseEntity<UserInfoDTO> {
        return ResponseEntity.ok().body(userService.getProfile(userId))
    }

    @PutMapping("/{userId}/email")
    fun updateUserEmail(
        @PathVariable userId: Long,
        @Valid @RequestBody changeEmailRequest: ChangeEmailRequest,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<AuthResponse> {
        val authResponse: AuthResponse = userService.changeEmail(
            userId, changeEmailRequest, request, response
        )

        return ResponseEntity.ok(authResponse)
    }

    @PutMapping("/{userId}/password")
    fun updateUserPassword(
        @PathVariable userId: Long,
        @Valid @RequestBody changePasswordRequestDTO: ChangePasswordRequestDTO
    ) {
        userService.changePassword(userId, changePasswordRequestDTO)
    }

    @PutMapping("/{userId}/details")
    fun updateUserBasicDetails(
        @PathVariable userId: Long,
        @Valid @RequestBody updateBasicDetailsRequest: UpdateBasicDetailsRequest
    ): UserInfoDTO {
        return userService.updateBasicDetails(userId, updateBasicDetailsRequest)
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('USER')")
    fun updateAccountStatus(
        @PathVariable userId: Long,
        @Valid @RequestBody accountStatusRequestDTO: AccountStatusRequestDTO
    ): UserInfoDTO {
        return userService.updateAccountStatus(userId, accountStatusRequestDTO)
    }

    @DeleteMapping("/{userId}")
    fun deleteUserAccount(@PathVariable userId: Long): ResponseEntity<Void> {
        userService.deleteAccount(userId)
        return ResponseEntity.ok().build()
    }


}