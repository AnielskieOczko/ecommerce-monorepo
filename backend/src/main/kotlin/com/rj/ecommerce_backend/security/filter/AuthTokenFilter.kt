package com.rj.ecommerce_backend.security.filter

import com.rj.ecommerce_backend.security.service.JwtBlackListService
import com.rj.ecommerce_backend.security.util.JwtUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

// Logger at the file level or in a companion object
private val logger = KotlinLogging.logger {}

@Component // Spring component annotation
class AuthTokenFilter( // Dependencies injected via primary constructor
    private val jwtUtils: JwtUtils,
    private val userDetailsService: UserDetailsService, // Spring's UserDetailsService interface
    private val jwtBlacklistService: JwtBlackListService
) : OncePerRequestFilter() { // Extends Spring's OncePerRequestFilter

    override fun doFilterInternal(
        request: HttpServletRequest, // Non-nullable by default in Kotlin override
        response: HttpServletResponse, // Non-nullable
        filterChain: FilterChain // Non-nullable
    ) { // No throws ServletException, IOException needed in Kotlin signature if not explicitly caught and rethrown
        try {
            val jwt = jwtUtils.parseJwt(request) // Returns String?

            if (jwt.isNullOrBlank()) {
                logger.trace { "No JWT found in request. Proceeding with filter chain." }
                filterChain.doFilter(request, response)
                return
            }

            // Check if token is blacklisted BEFORE validating its signature/expiry
            // This is more efficient if a token is known to be invalid.
            if (jwtBlacklistService.isTokenBlackListed(jwt)) {
                logger.warn { "JWT token is blacklisted: ${jwt.take(15)}..." }
                // SC_UNAUTHORIZED (401) is appropriate. Spring Security's exception handling
                // further down the chain (if no auth is set) will also result in 401 if needed.
                // Or, you could set a specific response error and not proceed.
                // For now, just logging and letting it proceed to filterChain might be okay,
                // as validateJwtToken or lack of authentication will handle it.
                // However, explicitly denying access here is safer:
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token: Blacklisted")
                return
            }

            if (jwtUtils.validateJwtToken(jwt)) { // validateJwtToken should ideally not throw for expected validation failures
                val username = jwtUtils.getUsernameFromJwtToken(jwt) // Returns String?

                if (username.isNullOrBlank()) {
                    logger.warn { "JWT is valid but username claim is missing or blank: ${jwt.take(15)}..." }
                    // Proceeding without authentication as username is essential
                } else {
                    try {
                        // Load user details
                        val userDetails: UserDetails = userDetailsService.loadUserByUsername(username)

                        // Create authentication token
                        val authentication = UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Credentials are not needed for JWT based auth after validation
                            userDetails.authorities // Get authorities from UserDetails
                        )

                        // Set details from the request
                        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

                        // Set the authentication in the security context
                        SecurityContextHolder.getContext().authentication = authentication
                        logger.debug { "Successfully authenticated user '$username' from JWT." }

                    } catch (e: UsernameNotFoundException) {
                        logger.warn { "User '$username' found in JWT but not found in UserDetailsService: ${e.message}" }
                        // Do not set authentication, let the filter chain proceed without it.
                        // Subsequent security checks will deny access if authentication is required.
                    } catch (e: Exception) {
                        // Catch other potential errors during UserDetails loading or authentication setup
                        logger.error { "Error setting user authentication for '$username' from JWT." }
                    }
                }
            } else {
                logger.debug { "JWT token validation failed for token: ${jwt.take(15)}..." }
                // Token is present but invalid (expired, bad signature, etc.).
                // Do not set authentication. Proceeding will likely result in 401 if auth is required.
            }
        } catch (e: Exception) {
            // This outer catch is for unexpected errors in the filter logic itself (e.g., from parseJwt).
            logger.error { "Cannot set user authentication: An unexpected error occurred in AuthTokenFilter." }
            // It's generally recommended to let Spring's error handling mechanisms deal with this.
            // Re-throwing might be appropriate if you have a higher-level filter error handler.
            // For now, just logging, as the filterChain.doFilter must be called.
        }

        filterChain.doFilter(request, response) // Always call this to continue the chain
    }
}