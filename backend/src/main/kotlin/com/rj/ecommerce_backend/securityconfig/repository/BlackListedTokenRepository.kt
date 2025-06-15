package com.rj.ecommerce_backend.securityconfig.repository

import com.rj.ecommerce.api.shared.dto.security.TokenInfo
import com.rj.ecommerce_backend.securityconfig.domain.BlacklistedToken
import org.antlr.v4.runtime.Token
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface BlackListedTokenRepository : JpaRepository<BlacklistedToken, Long> {

    @Query("SELECT b FROM BlacklistedToken b WHERE b.expiresAt <= :expiryDate")
    fun findExpiredTokens(@Param("expiryDate") expiryDate: LocalDateTime): List<BlacklistedToken>
    // Or: fun findByExpiresAtLessThanEqual(expiryDate: LocalDateTime): List<BlacklistedToken> // Derived query

    @Modifying
    @Query("DELETE FROM BlacklistedToken b WHERE b.expiresAt <= :expiryDate")
    fun deleteExpiredTokens(@Param("expiryDate") expiryDate: LocalDateTime): Int

    @Query("SELECT b FROM BlackListedToken b WHERE b.userId == :userId")
    fun findByUserId(@Param("userId") userId: Long): List<BlacklistedToken>

    fun existsByTokenString(token: String): Boolean
}