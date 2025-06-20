package com.rj.ecommerce_backend.securityconfig.service // Or your chosen Kotlin package

import com.rj.ecommerce_backend.securityconfig.services.JwtBlacklistService
import com.rj.ecommerce_backend.securityconfig.utils.JwtUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.logout.LogoutHandler
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class LogoutService(
    private val jwtUtils: JwtUtils,
    private val jwtBlacklistService: JwtBlacklistService
) : LogoutHandler {

    override fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication?
    ) {
        val authHeader = request.getHeader("Authorization")
        logger.debug { "Processing logout request. Auth header: ${authHeader?.take(15)}..." }

        val token = jwtUtils.parseJwt(request)

        if (token.isNullOrBlank()) {
            logger.info { "No JWT found in request, clearing security context anyway for logout." }
            SecurityContextHolder.clearContext()
            return
        }

        val usernameFromToken = jwtUtils.getUsernameFromJwtToken(token)
            ?: authentication?.name
            ?: "unknown_or_expired_token_user"

        try {
            jwtBlacklistService.blacklistToken(token, usernameFromToken)
            logger.info {
                "Token successfully blacklisted for user: '$usernameFromToken' (Token starting with: ${
                    token.take(
                        8
                    )
                }...)"
            }
        } catch (e: Exception) {
            logger.error(e) {
                "Failed to blacklist token for user: '$usernameFromToken' (Token starting with: ${
                    token.take(
                        8
                    )
                }...). Proceeding with logout."
            }
        } finally {
            SecurityContextHolder.clearContext()
            logger.debug { "Security context cleared for logout request involving user: '$usernameFromToken'." }
        }
    }
}