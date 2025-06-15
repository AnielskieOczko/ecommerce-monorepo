package com.rj.ecommerce_backend.securityconfig.service

import com.rj.ecommerce.api.shared.dto.security.AuthResponseDTO
import com.rj.ecommerce.api.shared.dto.security.JwtResponseDTO
import com.rj.ecommerce.api.shared.dto.security.LoginRequestDTO
import com.rj.ecommerce.api.shared.dto.security.TokenRefreshRequestDTO
import com.rj.ecommerce_backend.securityconfig.domain.RefreshToken
import com.rj.ecommerce_backend.securityconfig.exceptions.TokenRefreshException
import com.rj.ecommerce_backend.securityconfig.exceptions.UserAuthenticationException
import com.rj.ecommerce_backend.securityconfig.utils.JwtUtils
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

    companion object {
        private const val AUTH_HEADER = "Authorization"
        private const val TOKEN_PREFIX = "Bearer "
    }

    override fun authenticateUser(loginRequest: LoginRequestDTO): AuthResponseDTO {
        logger.info { "Attempting to authenticate user: ${loginRequest.email}" }
        return try {
            val authentication: Authentication = performAuthentication(loginRequest)
            val jwtResponse: JwtResponseDTO = generateAuthResponse(authentication)
            logger.info { "User authenticated successfully: ${loginRequest.email}" }
            AuthResponseDTO(
                success = true,
                message = "Authentication successful",
                data = jwtResponse
            )
        } catch (e: UserAuthenticationException) {
            logger.warn(e) { "Authentication failed for user ${loginRequest.email}: ${e.message}" }
            AuthResponseDTO(
                success = false,
                message = e.localizedMessage ?: "Authentication failed.", // Use exception's message
                data = null
            )
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during authentication for user ${loginRequest.email}" }
            AuthResponseDTO(
                success = false,
                message = "An unexpected error occurred during authentication.",
                data = null
            )
        }
    }

    override fun refreshToken(tokenRefreshRequest: TokenRefreshRequestDTO): AuthResponseDTO {
        logger.info { "Attempting to refresh token." }
        return try {
            val refreshToken: RefreshToken = refreshTokenService
                .verifyRefreshToken(tokenRefreshRequest.refreshToken)
            val jwtResponse: JwtResponseDTO = generateNewTokens(user = refreshToken.user)
            logger.info { "Token refreshed successfully for user ID: ${refreshToken.user.id}" }
            AuthResponseDTO(
                success = true,
                message = "Token refresh successful",
                data = jwtResponse
            )
        } catch (e: TokenRefreshException) {
            logger.warn(e) { "Token refresh failed: ${e.message}" }
            AuthResponseDTO(
                success = false,
                message = e.localizedMessage ?: "Token refresh failed.",
                data = null
            )
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during token refresh." }
            AuthResponseDTO(
                success = false,
                message = "An unexpected error occurred during token refresh.",
                data = null
            )
        }
    }

    override fun handleEmailUpdate(
        user: User,
        currentPassword: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): AuthResponseDTO {
        val userEmail = user.email.value // Cache for logging
        logger.info { "Attempting to handle email update for user: $userEmail" }
        return try {
            val userId = user.id
                ?: run {
                    logger.error { "User ID is null during email update attempt for email: $userEmail" }
                    throw IllegalStateException("User ID cannot be null when updating email and generating tokens for user: $userEmail")
                }
            logger.debug { "User ID $userId obtained for email update." }

            logoutCurrentUser(request, response)
            logger.debug { "Current user logged out for email update process for user: $userEmail" }

            val newAuth: Authentication = authenticateWithNewEmail(userEmail, currentPassword)
            logger.debug { "Successfully re-authenticated user $userEmail with new credentials." }


            val newAccessToken = jwtUtils.generateJwtToken(authentication = newAuth)
            val newRefreshToken: RefreshToken = refreshTokenService.createRefreshToken(userId)
            logger.debug { "Generated new access and refresh tokens for user ID: $userId after email update." }


            // It's generally better to return the token in the response body rather than setting it as a header
            // in an API context, but if this is a requirement, it's fine.
            // response.setHeader(AUTH_HEADER, TOKEN_PREFIX + newAccessToken) // Consider if this is truly needed or if JwtResponse is sufficient

            val jwtResponse = JwtResponseDTO(
                token = newAccessToken,
                refreshToken = newRefreshToken.token,
                id = userId,
                email = userEmail,
                roles = extractRoles(newAuth),
                type = "Bearer"
            )

            logger.info { "Email update and re-authentication successful for user ID: $userId" }
            AuthResponseDTO(
                success = true,
                message = "Authentication updated successfully after email change.",
                data = jwtResponse
            )

        } catch (e: IllegalStateException) {
            logger.error(e) { "Error during email update for $userEmail: ${e.message}" }
            AuthResponseDTO(
                success = false,
                message = e.localizedMessage ?: "Failed to update authentication due to invalid user state.",
                data = null
            )
        } catch (e: UserAuthenticationException) {
            logger.warn(e) { "Re-authentication failed during email update for $userEmail: ${e.message}" }
            AuthResponseDTO(
                success = false,
                message = e.localizedMessage ?: "Failed to update authentication due to re-authentication failure.",
                data = null
            )
        }
        catch (e: Exception) {
            logger.error(e) { "Unexpected error during email update for $userEmail" }
            AuthResponseDTO(
                success = false,
                message = "Failed to update authentication due to an unexpected error.",
                data = null
            )
        }
    }


    private fun performAuthentication(loginRequest: LoginRequestDTO): Authentication {
        logger.debug { "Performing authentication for: ${loginRequest.email}" }
        try {
            val authentication: Authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    loginRequest.email,
                    loginRequest.password
                )
            )
            SecurityContextHolder.getContext().authentication = authentication // Set authentication in context
            logger.debug { "Authentication successful for: ${loginRequest.email}" }
            return authentication
        } catch (e: BadCredentialsException) {
            logger.warn { "Invalid credentials for user: ${loginRequest.email}" }
            throw UserAuthenticationException("Invalid email or password.", e)
        } catch (e: Exception) {
            logger.error(e) { "Generic authentication failure for user: ${loginRequest.email}" }
            throw UserAuthenticationException("Authentication failed for an unknown reason.", e)
        }
    }

    private fun generateAuthResponse(authentication: Authentication): JwtResponseDTO {
        val userDetails: UserDetailsImpl = authentication.principal as UserDetailsImpl
        logger.debug { "Generating auth response for user: ${userDetails.username}" }
        val accessToken: String = jwtUtils.generateJwtToken(authentication)
        val refreshToken: RefreshToken = refreshTokenService.createRefreshToken(userDetails.id)

        val roles: List<String> = userDetails.authorities.map { grantedAuthority ->
            grantedAuthority.authority
        }

        return JwtResponseDTO(
            token = accessToken,
            refreshToken = refreshToken.token,
            id = userDetails.id,
            email = userDetails.username, // Assuming username here is the email
            roles = roles,
            type = "Bearer" // Consistent with companion object constant if used
        )
    }

    private fun generateNewTokens(user: User): JwtResponseDTO {
        val userEmailForLog = user.email.value
        logger.debug { "Generating new tokens for user: $userEmailForLog" }
        try {
            val userId = user.id
                ?: run {
                    logger.error { "User ID is null when trying to generate new tokens for user: $userEmailForLog" }
                    throw IllegalStateException("User ID cannot be null when generating tokens for user: $userEmailForLog")
                }

            val userDetails: UserDetailsImpl = UserDetailsImpl.build(user)
            val authentication: Authentication = UsernamePasswordAuthenticationToken(
                userDetails,
                null, // No credentials needed for this internal authentication object for token generation
                userDetails.authorities
            )

            val accessToken: String = jwtUtils.generateJwtToken(authentication)
            val newRefreshToken: RefreshToken = refreshTokenService.createRefreshToken(userId)

            val roles: List<String> = userDetails.authorities.map { grantedAuthority ->
                grantedAuthority.authority
            }
            logger.debug { "Successfully generated new tokens for user ID: $userId" }
            return JwtResponseDTO(
                token = accessToken,
                refreshToken = newRefreshToken.token,
                id = userId,
                email = user.email.value, // email from User object
                roles = roles,
                type = "Bearer"
            )

        } catch (e: IllegalStateException) {
            throw TokenRefreshException("Failed to generate new tokens due to invalid user state: ${e.message}", e)
        }
        catch (e: Exception) {
            logger.error(e) { "Failed to generate new tokens for user $userEmailForLog" }
            throw TokenRefreshException("Failed to generate new tokens for user $userEmailForLog.", e)
        }
    }

    private fun logoutCurrentUser(request: HttpServletRequest, response: HttpServletResponse) {
        val currentAuth: Authentication? = SecurityContextHolder.getContext().authentication
        if (currentAuth != null && currentAuth.isAuthenticated && currentAuth.name != "anonymousUser") {
            logger.debug { "Logging out current authenticated user: ${currentAuth.name}" }
            logoutService.logout(request, response, currentAuth)
            SecurityContextHolder.clearContext() // Ensure context is cleared
        } else {
            logger.debug { "No current authenticated user found to logout, or user is anonymous." }
        }
    }

    private fun authenticateWithNewEmail(email: String, password: String): Authentication {
        logger.debug { "Authenticating with new email: $email" }
        try {
            val newAuth: Authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(email, password)
            )
            // Setting the new authentication in the context might be desired if subsequent operations
            // in the same request rely on it. Otherwise, it might not be strictly necessary if you're
            // just generating a token to be used by the client for future requests.
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

    private fun extractRoles(authentication: Authentication): List<String> {
        return authentication.authorities.map { grantedAuthority ->
            grantedAuthority.authority
        }
    }
}
