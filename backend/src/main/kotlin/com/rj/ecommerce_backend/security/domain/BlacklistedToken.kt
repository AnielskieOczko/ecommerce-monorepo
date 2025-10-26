package com.rj.ecommerce_backend.security.domain

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "blacklisted_tokens")
@EntityListeners(AuditingEntityListener::class)
class BlacklistedToken(
    @Column(name = "token_string", length = 500, nullable = false, unique = true)
    var tokenString: String,

    @Column(name = "token_blacklisted_timestamp", nullable = false, updatable = false)
    var tokenBlacklistedTimestamp: LocalDateTime = LocalDateTime.now(),

    @Column(name = "token_expires_at", nullable = false)
    var tokenExpiresAt: LocalDateTime,

    @Column(name = "user_id_associated_token")
    var userIdAssociatedWithToken: Long?,

    @Column(name = "blacklisting_agent")
    var blacklistingAgent: String?
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @CreationTimestamp
    @Column(name = "record_created_at", nullable = false, updatable = false)
    var recordCreatedAt: LocalDateTime? = null

    @UpdateTimestamp
    @Column(name = "record_updated_at", nullable = false)
    var recordUpdatedAt: LocalDateTime? = null

    @CreatedBy
    @Column(name = "record_created_by", updatable = false)
    var recordCreatedBy: String? = null

    @LastModifiedBy
    @Column(name = "record_last_modified_by")
    var recordLastModifiedBy: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as BlacklistedToken
        return if (id != null) id == other.id else tokenString == other.tokenString && other.id == null
    }

    override fun hashCode(): Int = id?.hashCode() ?: tokenString.hashCode()

    override fun toString(): String {
        return "BlacklistedToken(id=$id, tokenString='${tokenString.take(8)}...', userIdAssociatedToken=$userIdAssociatedWithToken, tokenBlacklistedTimestamp=$tokenBlacklistedTimestamp)"
    }
}