package com.rj.ecommerce_backend.securityconfig.service // Or your chosen package

import com.rj.ecommerce.api.shared.core.Email
import com.rj.ecommerce_backend.securityconfig.services.UserDetailsImpl
import com.rj.ecommerce_backend.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class UserDetailsServiceImpl(
    private val userRepository: UserRepository
) : UserDetailsService {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(emailString: String?): UserDetails {
        // Validate input emailString
        if (emailString.isNullOrBlank()) {
            logger.warn { "Attempted to load user by null or blank email string." }
            throw UsernameNotFoundException("Email (username) cannot be null or blank.")
        }

        val emailVO: Email = try {
            Email.of(emailString)
        } catch (e: IllegalArgumentException) {
            logger.warn(e) { "Invalid email format provided: '$emailString'" }
            throw UsernameNotFoundException("Invalid email format: '$emailString'", e)
        }

        logger.debug { "Attempting to load user by email: ${emailVO.value}" }

        val user = userRepository.findUserByEmail(emailVO)
            ?: run {
                logger.warn { "User not found with email: ${emailVO.value}" }
                throw UsernameNotFoundException("User not found with email: ${emailVO.value}")
            }

        return UserDetailsImpl.build(user)
    }
}