package com.rj.ecommerce_backend.securityconfig.utils

import com.rj.ecommerce_backend.securityconfig.config.JwtConfig
import com.rj.ecommerce_backend.securityconfig.service.UserDetailsImpl
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SecurityException
import jakarta.servlet.http.HttpServletRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import java.security.Key
import java.util.Date

private val logger = KotlinLogging.logger {}

@Component
class JwtUtils(
    private val jwtConfig: JwtConfig
) {

    private val jwtSecret: String get() = jwtConfig.secret
    private val jwtTokenExpirationMs: Int get() = jwtConfig.expirationMs
    private val signingKey: Key by lazy {
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret))
    }

    fun generateJwtToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as? UserDetailsImpl
            ?: throw IllegalArgumentException("Authentication principal is not an instance of UserDetailsImpl")

        logger.info { "Generating JWT for user ID: ${userPrincipal.id}, email: ${userPrincipal.username}" }
        logger.debug { "Using JWT Expiration (from config): $jwtTokenExpirationMs ms" }

        val authorities = userPrincipal.authorities
            .map(GrantedAuthority::getAuthority)

        val now = Date()
        val expiryDate = Date(now.time + jwtTokenExpirationMs)

        return Jwts.builder()
            .setSubject(userPrincipal.id.toString())
            .claim("username", userPrincipal.username)
            .claim("authorities", authorities)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(signingKey, SignatureAlgorithm.HS512)
            .compact()
    }

    fun getTokenClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .body
    }

    fun getUsernameFromJwtToken(token: String): String? {
        return try {
            getTokenClaims(token).get("username", String::class.java)
        } catch (e: Exception) {
            logger.warn(e) { "Could not get username from JWT: Token might be invalid or claim missing." }
            null
        }
    }

    fun getUserIdFromJwtToken(token: String): Long? {
        return try {
            getTokenClaims(token).subject?.toLongOrNull()
        } catch (e: Exception) {
            logger.warn(e) { "Could not get user ID from JWT: Token might be invalid." }
            null
        }
    }

    fun parseJwt(request: HttpServletRequest): String? {
        val headerAuth = request.getHeader("Authorization")
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ", ignoreCase = true)) {
            return headerAuth.substring(7)
        }
        return null
    }

    fun validateJwtToken(authToken: String?): Boolean {
        if (authToken.isNullOrBlank()) {
            logger.debug { "JWT validation failed: Token was null or blank." }
            return false
        }
        try {
            Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(authToken)
            return true
        } catch (e: SecurityException) {
            logger.warn { "Invalid JWT signature: ${e.message}" }
        } catch (e: MalformedJwtException) {
            logger.warn { "Invalid JWT token: ${e.message}" }
        } catch (e: ExpiredJwtException) {
            logger.warn { "JWT token is expired: ${e.message}" }
        } catch (e: UnsupportedJwtException) {
            logger.warn { "JWT token is unsupported: ${e.message}" }
        } catch (e: IllegalArgumentException) {
            logger.warn { "JWT claims string is empty or argument is invalid: ${e.message}" }
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error validating JWT token."}
        }
        return false
    }
}