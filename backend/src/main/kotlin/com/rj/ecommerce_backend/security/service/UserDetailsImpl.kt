package com.rj.ecommerce_backend.security.service

import com.rj.ecommerce_backend.user.domain.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Spring Security UserDetails implementation for representing an authenticated user principal.
 * 
 * This class wraps user authentication data and provides Spring Security with the necessary
 * information for authorization and security context management.
 *
 * @property id The unique identifier of the user
 * @property username The username (email) used for authentication
 * @property password The encoded password
 * @property authorities The collection of granted authorities/roles
 */
data class UserDetailsImpl(
    val id: Long,
    private val username: String,
    private val password: String,
    private val authorities: Collection<GrantedAuthority>
) : UserDetails {

    companion object {
        /**
         * Factory method to build UserDetailsImpl from a User domain entity.
         *
         * @param user The User domain entity
         * @return UserDetailsImpl instance with mapped authorities
         */
        fun build(user: User): UserDetailsImpl {
            val authorities = user.authorities.map { authority ->
                SimpleGrantedAuthority(authority.name)
            }

            return UserDetailsImpl(
                id = user.id ?: throw IllegalArgumentException("User ID cannot be null"),
                username = user.email.value, // Use email as username
                password = user.password.value,
                authorities = authorities
            )
        }
    }

    // Spring Security UserDetails interface methods
    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getPassword(): String = password

    override fun getUsername(): String = username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    // Override toString to avoid exposing sensitive information
    override fun toString(): String {
        return "UserDetailsImpl(id=$id, username=$username, authorities=${authorities.map { it.authority }})"
    }
}
