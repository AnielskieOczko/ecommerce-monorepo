package com.rj.ecommerce_backend.securityconfig.controller

import com.rj.ecommerce.api.shared.dto.security.AuthResponseDTO
import com.rj.ecommerce.api.shared.dto.security.TokenInfoDTO
import com.rj.ecommerce.api.shared.dto.security.LoginRequestDTO
import com.rj.ecommerce.api.shared.dto.security.TokenRefreshRequestDTO
import com.rj.ecommerce.api.shared.dto.user.UserCreateRequestDTO
import com.rj.ecommerce.api.shared.dto.user.UserInfoDTO
import com.rj.ecommerce_backend.securityconfig.service.AuthenticationService
import com.rj.ecommerce_backend.securityconfig.service.JwtBlackListService
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
        @Valid @RequestBody createUserRequest: UserCreateRequestDTO
    ): ResponseEntity<UserInfoDTO> {
        // It's good to log the incoming request, but be careful about logging sensitive PII.
        // For createUserRequest, email is PII. Consider logging only a confirmation or non-sensitive parts.
        logger.info { "Received request to register user with email: ${createUserRequest.email}" } // Assuming email is a primary identifier here
        return try {
            val createdUser = adminService.createUser(createUserRequest)
            logger.info { "User registered successfully with ID: ${createdUser.id} and email: ${createdUser.email}" }
            ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUser)
        } catch (e: Exception) { // Catching a more specific exception from adminService would be better if available
            logger.error(e) { "Error during user registration for email: ${createUserRequest.email}" }
            // Consider what status to return. If it's a validation error already caught by @Valid,
            // a global exception handler (@ControllerAdvice) might be better.
            // If it's a duplicate email, HttpStatus.CONFLICT (409) might be more appropriate.
            // For a generic server error:
            throw e // Re-throw to be handled by a global exception handler, or return a ResponseEntity like below
            // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build() // Or a more descriptive error DTO
        }
    }

    @PostMapping("/login")
    fun authenticateUser(
        @Valid @RequestBody loginRequest: LoginRequestDTO
    ): ResponseEntity<AuthResponseDTO> {
        logger.info { "Received login request for user: ${loginRequest.email}" }
        // The AuthenticationService.authenticateUser method already handles exceptions and returns an AuthResponse.
        // So, the try-catch here might be redundant or could be simplified.
        // Let's assume authenticationService.authenticateUser can throw exceptions that are NOT UserAuthenticationException
        // (though it's designed to wrap them) or that you want to handle AuthResponse success=false specifically.

        val authResponse: AuthResponseDTO = authenticationService.authenticateUser(loginRequest)

        return if (authResponse.success) {
            logger.info { "User login successful for: ${loginRequest.email}" }
            ResponseEntity.ok(authResponse)
        } else {
            // The service already logged the warning, but we can add a controller-level log.
            logger.warn { "User login failed for: ${loginRequest.email}. Reason: ${authResponse.message}" }
            // Return 401 Unauthorized for login failures
            ResponseEntity
                .status(HttpStatus.UNAUTHORIZED) // More appropriate for login failure
                .body(authResponse) // The service already crafted this error response
        }
        // The original try-catch block can be removed if the service layer handles all relevant exceptions
        // and returns an AuthResponse. If not, and the service can throw other exceptions that need specific handling:
        /*
        try {
            val authResponse: AuthResponse = authenticationService.authenticateUser(loginRequest)
            if (authResponse.success) {
                logger.info { "User login successful for: ${loginRequest.email}" }
                return ResponseEntity.ok(authResponse)
            } else {
                logger.warn { "Authentication failed for user ${loginRequest.email} as reported by service: ${authResponse.message}" }
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED) // Use 401 for authentication failures
                    .body(authResponse) // The service already prepared this
            }
        } catch (e: UserAuthenticationException) { // This might be redundant if service already handles it
            logger.warn(e) { "Explicit UserAuthenticationException during login for ${loginRequest.email}: ${e.message}" }
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                    AuthResponse(
                        success = false,
                        message = e.localizedMessage ?: "Authentication failed.",
                        data = null
                    )
                )
        } catch (e: Exception) { // Catch all for unexpected server errors
            logger.error(e) { "Unexpected error during login for ${loginRequest.email}" }
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    AuthResponse(
                        success = false,
                        message = "An unexpected error occurred. Please try again.",
                        data = null
                    )
                )
        }
        */
    }

    @GetMapping("/user/{userId}/tokens")
    fun getUserTokens(@PathVariable userId: Long): ResponseEntity<List<TokenInfoDTO>> {
        logger.info { "Received request to get tokens for user ID: $userId" }
        return try {
            val tokens = jwtBlackListService.getUserTokens(userId)
            logger.debug { "Successfully retrieved ${tokens.size} tokens for user ID: $userId" }
            ResponseEntity.ok(tokens)
        } catch (e: Exception) { // Catch specific exceptions if JwtBlackListService can throw them
            logger.error(e) { "Error retrieving tokens for user ID: $userId" }
            // Consider what to return. An empty list might be acceptable if the user has no tokens or doesn't exist,
            // or a 404 if the user ID is not found and that's an error condition.
            // For a generic server error:
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emptyList()) // Or a proper error DTO
        }
    }

    @PostMapping("/refresh-token")
    fun refreshToken(@Valid @RequestBody request: TokenRefreshRequestDTO): ResponseEntity<AuthResponseDTO> {
        // Avoid logging the full refresh token for security reasons. Log its presence or a part of it if necessary.
        logger.info { "Received request to refresh token." } // Potentially add request.userIdentifier if available and safe
        // Similar to /login, AuthenticationService.refreshToken should handle its errors and return an AuthResponse.
        val authResponse = authenticationService.refreshToken(request)

        return if (authResponse.success) {
            logger.info { "Token refresh successful." } // Add user identifier if available in response
            ResponseEntity.ok(authResponse)
        } else {
            logger.warn { "Token refresh failed. Reason: ${authResponse.message}" }
            // TokenRefreshException could lead to 401 (if token is invalid/expired) or 403 (if token is valid but not permitted for refresh)
            // The service layer might already set an appropriate message.
            ResponseEntity
                .status(HttpStatus.UNAUTHORIZED) // Or HttpStatus.FORBIDDEN depending on the nature of TokenRefreshException
                .body(authResponse)
        }

    }
}