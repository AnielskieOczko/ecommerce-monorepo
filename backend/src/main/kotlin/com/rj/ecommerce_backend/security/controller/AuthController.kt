package com.rj.ecommerce_backend.security.controller

import com.rj.ecommerce.api.shared.dto.security.request.LoginRequest
import com.rj.ecommerce.api.shared.dto.security.request.TokenRefreshRequest
import com.rj.ecommerce.api.shared.dto.security.response.AuthResponse
import com.rj.ecommerce.api.shared.dto.security.response.TokenInfoResponse
import com.rj.ecommerce.api.shared.dto.user.request.UserCreateRequest
import com.rj.ecommerce.api.shared.dto.user.response.UserResponse
import com.rj.ecommerce_backend.security.service.AuthenticationService
import com.rj.ecommerce_backend.security.service.JwtBlackListService
import com.rj.ecommerce_backend.user.service.AdminService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger { }

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class AuthController(
    private val authenticationService: AuthenticationService,
    private val jwtBlackListService: JwtBlackListService,
    private val adminService: AdminService
) {

    @PostMapping("/register")
    fun registerUser(
        @Valid @RequestBody createUserRequest: UserCreateRequest
    ): ResponseEntity<UserResponse> {
        logger.info { "Received request to register user with email: ${createUserRequest.email}" }

        val createdUser = adminService.createUser(createUserRequest)

        logger.info { "User registered successfully with ID: ${createdUser.id}" }
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser)
    }

    @PostMapping("/login")
    fun authenticateUser(
        @Valid @RequestBody loginRequest: LoginRequest
    ): ResponseEntity<AuthResponse> {
        logger.info { "Received login request for user: ${loginRequest.email}" }

        val authResponse = authenticationService.authenticateUser(loginRequest)

        logger.info { "User login successful for: ${loginRequest.email}" }
        return ResponseEntity.ok(authResponse)
    }

    @GetMapping("/user/{userId}/tokens")
    fun getUserTokens(@PathVariable userId: Long): ResponseEntity<List<TokenInfoResponse>> {
        logger.info { "Received request to get tokens for user ID: $userId" }

        val tokens = jwtBlackListService.getUserTokens(userId)

        logger.debug { "Successfully retrieved ${tokens.size} tokens for user ID: $userId" }
        return ResponseEntity.ok(tokens)
    }

    @PostMapping("/refresh-token")
    fun refreshToken(
        @Valid @RequestBody request: TokenRefreshRequest
    ): ResponseEntity<AuthResponse> {
        logger.info { "Received request to refresh token." }

        val authResponse = authenticationService.refreshToken(request)

        logger.info { "Token refresh successful." }
        return ResponseEntity.ok(authResponse)
    }
}