package com.rj.ecommerce_backend.securityconfig

import com.rj.ecommerce.api.shared.core.Email
import com.rj.ecommerce_backend.user.domain.User
import com.rj.ecommerce_backend.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class SecurityContextImpl(private val userRepository: UserRepository) : SecurityContext {

    override fun ensureAccess(targetUserId: Long) {
        val currentUser = getCurrentUser()

        if (!isAdmin() && currentUser.id != targetUserId) {
            logger.warn { "User ${currentUser.id} attempted to access resource for user $targetUserId without admin rights." }
            throw AccessDeniedException("You do not have permission to access this resource.")
        }
    }

    override fun getCurrentUser(): User {
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication == null || !authentication.isAuthenticated || authentication.name == null) {
            logger.warn { "Attempt to get current user with no valid authentication." }

            throw UsernameNotFoundException("No authenticated user found in security context.")
        }
        val username = authentication.name

        return userRepository.findUserByEmail(Email(username))
            .orElseThrow {
                logger.warn { "User not found in repository for authenticated username: $username" }
                UsernameNotFoundException("User details not found for authenticated user: $username")
            }
    }

    override fun isAdmin(): Boolean {
        return getCurrentUser().authorities.any { authority ->
            "ROLE_ADMIN" == authority.name
        }
    }

    override fun ensureAdmin() {
        if (!isAdmin()) {
            logger.warn { "Admin access denied for user: ${getCurrentUser().id}" }
            throw AccessDeniedException("Access Denied: Admin privileges required.")
        }
    }
}