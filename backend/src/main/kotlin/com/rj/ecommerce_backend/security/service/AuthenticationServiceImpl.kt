package com.rj.ecommerce_backend.security.service

import com.rj.ecommerce.api.shared.dto.security.request.LoginRequest
import com.rj.ecommerce.api.shared.dto.security.request.TokenRefreshRequest
import com.rj.ecommerce.api.shared.dto.security.response.AuthResponse
import com.rj.ecommerce_backend.security.exception.UserAuthenticationException
import com.rj.ecommerce_backend.security.util.JwtUtils
import com.rj.ecommerce_backend.user.domain.User
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger { }

@Service
@Transactional
class AuthenticationServiceImpl(
    private val authenticationManager: AuthenticationManager,
    private val jwtUtils: JwtUtils,
    private val refreshTokenService: RefreshTokenService,
    private val logoutService: LogoutService
) : AuthenticationService {

    override fun authenticateUser(loginRequest: LoginRequest): AuthResponse {
        logger.info { "Attempting to authenticate user: ${loginRequest.email}" }
        val authentication = performAuthentication(loginRequest)
        logger.info { "User authenticated successfully: ${loginRequest.email}" }
        return generateAndPersistTokens(authentication)
    }

    override fun refreshToken(tokenRefreshRequest: TokenRefreshRequest): AuthResponse {
        logger.info { "Attempting to refresh token." }
        val refreshToken = refreshTokenService.verifyRefreshToken(tokenRefreshRequest.refreshToken)
        val newAuthResponse = generateNewTokensForUser(refreshToken.user)
        logger.info { "Token refreshed successfully for user ID: ${refreshToken.user.id}" }
        return newAuthResponse
    }

    override fun handleEmailUpdate(
        user: User,
        currentPassword: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): AuthResponse {
        val userEmail = user.email.value
        logger.info { "Attempting to handle email update for user: $userEmail" }

        val userId = user.id
            ?: throw IllegalStateException("User ID cannot be null when updating email for user: $userEmail")

        logoutCurrentUser(request, response)
        val newAuth = authenticateWithNewEmail(userEmail, currentPassword)

        logger.info { "Email update and re-authentication successful for user ID: $userId" }
        return generateNewTokensForUser(user)
    }

    private fun performAuthentication(loginRequest: LoginRequest): Authentication {
        try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(loginRequest.email, loginRequest.password)
            )
            SecurityContextHolder.getContext().authentication = authentication
            return authentication
        } catch (e: BadCredentialsException) {
            logger.warn { "Invalid credentials for user: ${loginRequest.email}" }
            throw UserAuthenticationException("Invalid email or password.", e)
        } catch (e: Exception) {
            logger.error(e) { "Generic authentication failure for user: ${loginRequest.email}" }
            throw UserAuthenticationException("Authentication failed for an unknown reason.", e)
        }
    }

    private fun generateAndPersistTokens(authentication: Authentication): AuthResponse {
        val userDetails = authentication.principal as UserDetailsImpl
        logger.debug { "Generating and persisting tokens for user: ${userDetails.username}" }

        val accessToken = jwtUtils.generateJwtToken(authentication)
        val refreshToken = refreshTokenService.createRefreshToken(userDetails.id)

        return AuthResponse(
            token = accessToken,
            refreshToken = refreshToken.token,
            id = userDetails.id,
            email = userDetails.username,
            roles = userDetails.authorities.map { it.authority }
        )
    }

    private fun generateNewTokensForUser(user: User): AuthResponse {
        val userId = user.id ?: throw IllegalStateException("Cannot generate tokens for a user with a null ID.")
        logger.debug { "Generating new tokens for user ID: $userId" }

        val userDetails = UserDetailsImpl.build(user)
        val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)

        val newAccessToken = jwtUtils.generateJwtToken(authentication)
        val newRefreshToken = refreshTokenService.createRefreshToken(userId)

        return AuthResponse(
            token = newAccessToken,
            refreshToken = newRefreshToken.token,
            id = userId,
            email = user.email.value,
            roles = userDetails.authorities.map { it.authority }
        )
    }

    private fun logoutCurrentUser(request: HttpServletRequest, response: HttpServletResponse) {
        val currentAuth = SecurityContextHolder.getContext().authentication
        if (currentAuth != null && currentAuth.isAuthenticated && currentAuth.name != "anonymousUser") {
            logger.debug { "Logging out current authenticated user: ${currentAuth.name}" }
            logoutService.logout(request, response, currentAuth)
            SecurityContextHolder.clearContext()
        } else {
            logger.debug { "No current authenticated user found to logout." }
        }
    }

    private fun authenticateWithNewEmail(email: String, password: String): Authentication {
        try {
            val newAuth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(email, password)
            )
            SecurityContextHolder.getContext().authentication = newAuth
            logger.debug { "Successfully authenticated with new email: $email and set in SecurityContext." }
            return newAuth
        } catch (e: BadCredentialsException) {
            logger.warn { "Invalid credentials during re-authentication for email: $email" }
            throw UserAuthenticationException("Invalid credentials provided for email update.", e)
        } catch (e: Exception) {
            logger.error(e) { "Authentication failed for new email $email" }
            throw UserAuthenticationException("Authentication failed with new email.", e)
        }
    }
}