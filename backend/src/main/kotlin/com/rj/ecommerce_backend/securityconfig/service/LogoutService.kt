package com.rj.ecommerce_backend.securityconfig.service // Or your chosen Kotlin package

import com.rj.ecommerce_backend.securityconfig.services.JwtBlacklistService
import com.rj.ecommerce_backend.securityconfig.utils.JwtUtils // Your Kotlin JwtUtils
// Import JwtBlacklistService if it's defined and in a different package
// import com.rj.ecommerce_backend.securityconfig.service.JwtBlacklistService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.logout.LogoutHandler // Spring Security interface
import org.springframework.stereotype.Service

// Logger at the file level or in a companion object
private val logger = KotlinLogging.logger {}

@Service // Spring annotation for service component
class LogoutService( // Dependencies injected via primary constructor
    private val jwtUtils: JwtUtils,
    private val jwtBlacklistService: JwtBlacklistService // Assuming this service exists
) : LogoutHandler { // Implements Spring Security interface

    override fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication? // Authentication can be null if logout is called on an unauthenticated session
    ) {
        val authHeader = request.getHeader("Authorization")
        logger.debug { "Processing logout request. Auth header: ${authHeader?.take(15)}..." } // Log only part of header

        val token = jwtUtils.parseJwt(request) // This already handles "Bearer " prefix and null header

        if (token.isNullOrBlank()) {
            logger.info { "No JWT found in request, clearing security context anyway for logout." }
            SecurityContextHolder.clearContext() // Clear context even if no token (e.g., session cookie logout)
            return
        }

        // Attempt to get username from token for logging/auditing, even if token might be invalid/expired
        // jwtUtils.getUsernameFromJwtToken already handles invalid tokens gracefully by returning null.
        val usernameFromToken = jwtUtils.getUsernameFromJwtToken(token)
            ?: authentication?.name // Fallback to username from Authentication principal if available
            ?: "unknown_or_expired_token_user"

        try {
            // Blacklist the token
            jwtBlacklistService.blacklistToken(token, usernameFromToken)
            logger.info { "Token successfully blacklisted for user: '$usernameFromToken' (Token starting with: ${token.take(8)}...)" }
        } catch (e: Exception) {
            // Log failure to blacklist but still proceed with clearing context for security
            logger.error(e) { "Failed to blacklist token for user: '$usernameFromToken' (Token starting with: ${token.take(8)}...). Proceeding with logout." }
        } finally {
            // Always clear the security context on logout
            SecurityContextHolder.clearContext()
            logger.debug { "Security context cleared for logout request involving user: '$usernameFromToken'." }
        }
    }
}