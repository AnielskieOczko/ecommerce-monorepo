package com.rj.ecommerce_backend.security.service

import com.rj.ecommerce.api.shared.dto.security.response.TokenInfoResponse
import com.rj.ecommerce_backend.security.domain.BlacklistedToken
import com.rj.ecommerce_backend.security.exception.TokenBlacklistException
import com.rj.ecommerce_backend.security.repository.BlackListedTokenRepository
import com.rj.ecommerce_backend.security.util.JwtUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import jakarta.transaction.Transactional
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

private val logger = KotlinLogging.logger { }

@Service
@Transactional
class JwtBlackListServiceImpl(
    private val blackListedTokenRepository: BlackListedTokenRepository,
    private val jwtUtils: JwtUtils
) : JwtBlackListService {
    override fun blackListToken(token: String, username: String) {
        try {

            val claims: Claims = jwtUtils.getTokenClaims(token)
            val userId: Long? = claims.subject.toLongOrNull()
            val expirationDateFromClaims: Date? = claims.expiration

            val tokenExpiresAtDateTime: LocalDateTime = expirationDateFromClaims?.let { date ->
                LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
            } ?: run {
                // Handle case where expiration date is null - this might be an invalid token
                logger.error { "Token for user '$username' is missing an expiration date. Cannot blacklist effectively." }
                // Depending on policy, you might throw an exception or use a default far-future date,
                // but using the actual expiration is key for cleanup.
                // For now, let's throw, as blacklisting without a proper expiry might be problematic.
                throw TokenBlacklistException("Token for user '$username' has no expiration date.")
            }

            val blackListedToken = BlacklistedToken(
                tokenString = token,
                tokenBlacklistedTimestamp = LocalDateTime.now(),
                tokenExpiresAt = tokenExpiresAtDateTime,
                userIdAssociatedWithToken = userId,
                blacklistingAgent = username
            )

            blackListedTokenRepository.save(blackListedToken)
            logger.info { "Token blacklisted successfully for user: $username" }

        } catch (e: Error) {
            logger.error(e) { "Error blacklisting token for user: $username" }
            throw TokenBlacklistException("Failed to blacklist token for user $username.", e)
        }
    }

    override fun isTokenBlackListed(token: String): Boolean {
        return try {
            blackListedTokenRepository.existsByTokenString(token)
        } catch (e: Exception) {
            logger.error(e) { "Error checking if token '$token' is blacklisted. Assuming blacklisted for security." }
            true // Consistent with Java: fail-safe by considering it blacklisted
        }
    }

    override fun getUserTokens(userId: Long): List<TokenInfoResponse> {
        return blackListedTokenRepository.findByUserId(userId)
            .map { domainToken ->
                TokenInfoResponse(
                    token = domainToken.tokenString,
                    blacklistedAt = domainToken.recordCreatedAt,
                    expiresAt = domainToken.tokenExpiresAt,
                    blacklistedBy = domainToken.recordCreatedBy
                )
            }
    }

    @Scheduled(cron = "\${token.cleanup.cron:0 0 * * * *}")
    override fun cleanupExpiredTokens(): Int {
        try {
            val now = LocalDateTime.now()
            val deletedCount = blackListedTokenRepository.deleteExpiredTokens(now)
            logger.info { "Cleaned up $deletedCount expired tokens." }
            return deletedCount
        } catch (e: Exception) {
            logger.error(e) { "Error during token cleanup." }
            return 0
        }
    }

}