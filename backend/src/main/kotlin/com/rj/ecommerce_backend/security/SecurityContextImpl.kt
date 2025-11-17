package com.rj.ecommerce_backend.security

import com.rj.ecommerce_backend.api.shared.core.Email
import com.rj.ecommerce_backend.user.domain.User
import com.rj.ecommerce_backend.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import org.springframework.web.context.WebApplicationContext

private val logger = KotlinLogging.logger {}

/**
 * A request-scoped component that provides security context information.
 * It caches the User object for the duration of a single HTTP request
 * to prevent multiple database lookups.
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
class SecurityContextImpl(private val userRepository: UserRepository) : SecurityContext {

    // This property will cache the user for the lifetime of the request.
    private var userCache: User? = null

    /**
     * Retrieves the currently authenticated user.
     * On the first call within a request, it fetches the user from the database.
     * On subsequent calls, it returns the cached user instantly.
     *
     * @return A non-null User with a guaranteed non-null ID.
     * @throws UsernameNotFoundException if no authenticated user is found.
     * @throws IllegalStateException if the found user has a null ID (data integrity issue).
     */
    override fun getCurrentUser(): User {
        // If the user is already cached for this request, return it immediately.
        userCache?.let { return it }

        // Otherwise, fetch, validate, cache, and return the user.
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw UsernameNotFoundException("No authentication found in security context.")

        if (!authentication.isAuthenticated || authentication.name == null) {
            throw UsernameNotFoundException("User is not authenticated or has no name.")
        }

        val username = authentication.name
        val user = userRepository.findUserByEmail(Email(username))
            ?: throw UsernameNotFoundException("User details not found for authenticated user: $username")

        // The critical contract guarantee.
        requireNotNull(user.id) {
            "Authenticated user '$username' was found but has a null ID. This indicates a critical data integrity issue."
        }

        // Cache the user for subsequent calls in this same request.
        userCache = user
        return user
    }

    /**
     * Checks if the current user has ADMIN role. Uses the cached user object.
     */
    override fun isAdmin(): Boolean {
        // This no longer causes a second DB hit because getCurrentUser() is now cached.
        return getCurrentUser().authorities.any { "ROLE_ADMIN" == it.name }
    }

    /**
     * Ensures the current user is an admin, otherwise throws AccessDeniedException.
     */
    override fun ensureAdmin() {
        if (!isAdmin()) {
            // Log with the cached user's ID without another DB hit.
            logger.warn { "Admin access denied for user: ${getCurrentUser().id}" }
            throw AccessDeniedException("Access Denied: Admin privileges required.")
        }
    }

    /**
     * Ensures the current user can access a resource belonging to the targetUserId.
     */
    override fun ensureAccess(targetUserId: Long) {
        val currentUser = getCurrentUser() // Fetches from cache after the first call.
        if (!isAdmin() && currentUser.id != targetUserId) {
            logger.warn { "User ${currentUser.id} attempted to access resource for user $targetUserId without admin rights." }
            throw AccessDeniedException("You do not have permission to access this resource.")
        }
    }
}