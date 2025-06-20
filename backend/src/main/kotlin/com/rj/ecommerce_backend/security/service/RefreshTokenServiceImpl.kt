package com.rj.ecommerce_backend.securityconfig.service

import com.rj.ecommerce_backend.securityconfig.domain.RefreshToken
import com.rj.ecommerce_backend.securityconfig.exceptions.TokenRefreshException
import com.rj.ecommerce_backend.securityconfig.repository.RefreshTokenRepository
import com.rj.ecommerce_backend.user.domain.User
import com.rj.ecommerce_backend.user.exception.UserNotFoundException
import com.rj.ecommerce_backend.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

private val logger = KotlinLogging.logger { }

@Service
@Transactional
class RefreshTokenServiceImpl(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository,
    private val request: HttpServletRequest

) : RefreshTokenService {

    companion object {
        private final const val REFRESH_TOKEN_DURATION_SECONDS: Long = 8640000L
    }

    override fun createRefreshToken(userId: Long): RefreshToken {

        logger.info { "Creating new refresh token for user: $userId" }

        val user: User = userRepository.findById(userId).orElseThrow {
            UserNotFoundException("User with id $userId not found.")
        }

        refreshTokenRepository.deleteByUserId(userId)

        val refreshToken = RefreshToken(
            user = user,
            token = generateRefreshToken(),
            expiryDate = LocalDateTime.now()
                .plusMinutes(REFRESH_TOKEN_DURATION_SECONDS)
        )

        if (refreshToken.expiryDate.isBefore(LocalDateTime.now())) {
            TokenRefreshException("Refresh token was expired")
        }

        return refreshToken
    }

    override fun verifyRefreshToken(token: String): RefreshToken {
        val refreshToken: RefreshToken = refreshTokenRepository.findByTokenWithUser(token) ?: run {
            throw TokenRefreshException("Invalid refresh token.")
        }

        if (refreshToken.expiryDate.isBefore(LocalDateTime.now())) {
            TokenRefreshException("Refresh token was expired")
        }

        return refreshToken

    }

    private fun generateRefreshToken(): String {
        return java.util.UUID.randomUUID().toString()
    }

    private fun getClientIp(): String {
        val xfHeader: String? = request.getHeader("X-Forwarded-For")
        if (xfHeader == null) {
            return request.remoteAddr
        }

        return xfHeader.split(",")[0]
    }


}