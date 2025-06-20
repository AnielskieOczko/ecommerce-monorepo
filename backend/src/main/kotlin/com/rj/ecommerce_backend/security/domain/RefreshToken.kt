package com.rj.ecommerce_backend.securityconfig.domain

import com.rj.ecommerce_backend.user.domain.User
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.Objects // For Objects.hash in hashCode

@Entity
@Table(name = "refresh_tokens")
@EntityListeners(AuditingEntityListener::class)
class RefreshToken(

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    var user: User,

    @Column(nullable = false, unique = true, length = 512)
    var token: String,

    @Column(nullable = false)
    var expiryDate: LocalDateTime

) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null

    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null

    @CreatedBy
    @Column(updatable = false)
    var createdBy: String? = null

    @LastModifiedBy
    var lastModifiedBy: String? = null


    override fun equals(other: Any?): Boolean {
        if (this === other) return true // Are they the exact same instance in memory?

        // Is 'other' null? Is 'other' of the exact same class as 'this'?
        // Using javaClass ensures strict type checking, good for JPA entities to avoid issues with proxies.
        if (other == null || javaClass != other.javaClass) return false

        other as RefreshToken // Smart cast 'other' to RefreshToken

        // If 'id' is null for this object, it's transient (new, not saved).
        // Two different transient RefreshToken instances are generally not considered equal,
        // even if their other properties match, unless there's a strong natural/business key
        // that is set *before* persistence and guarantees uniqueness.
        // For refresh tokens, the 'token' string itself *could* be that key if generated
        // client-side or guaranteed unique before DB save.
        // However, the simplest and safest for JPA is often:
        // - Persisted entities are equal if their IDs are equal.
        // - Transient entities are only equal if they are the same instance (this === other).
        if (id == null) {
            // If you consider two new, unsaved tokens with the same 'token' string and user as equal:
            // return this.token == other.token && this.user.id == other.user.id && other.id == null
            // For now, let's stick to the common JPA pattern: transient objects are distinct unless identical instance.
            return false
        }

        // If 'id' is not null, then 'other.id' must also be not null and equal to this.id.
        return id == other.id
    }

    override fun hashCode(): Int {
        // If 'id' is set (entity is persisted or ID is assigned pre-persist), use its hashCode.
        // This ensures consistency with the 'equals' method.
        return if (id != null) {
            id.hashCode()
        } else {
            // For transient instances, if 'equals' considers them distinct unless they are the same instance,
            // then using System.identityHashCode(this) or a hash based on pre-persistence business keys is appropriate.
            // If 'token' and 'user.id' were used in 'equals' for transient, use them here:
            // return Objects.hash(token, user.id)

            // Given the simplified 'equals' for transient, using 'token' (if always set on creation)
            // or System.identityHashCode is reasonable. Using 'token' as it's a key part of the object.
            Objects.hash(token, user.id, expiryDate) // Hash based on initial constructor fields for consistency
            // if 'id' is not yet available. This helps if transient
            // objects are put into a Set BEFORE id is generated,
            // assuming these fields don't change for a given instance.
            // Be cautious if these fields ARE mutable and id is null.
            // A safer bet for purely transient objects that are only equal if
            // 'this === other' is to use System.identityHashCode or a fixed prime.
            // Let's use the primary constructor fields used to define the token.
        }
    }

    override fun toString(): String {
        // Avoid loading lazy 'user' fully. Use user.id.
        // Truncate token for brevity and security in logs.
        return "RefreshToken(" +
                "id=$id, " +
                "userId=${user.id}, " + // Access user.id (assuming User.id is accessible and non-null if User is set)
                "token='${token.take(8)}...', " +
                "expiryDate=$expiryDate, " +
                "createdAt=$createdAt" +
                ")"
    }

    // No-argument constructor is required by JPA.
    // The kotlin-jpa plugin will generate this for classes annotated with @Entity.
    // If not using the plugin, you would need to provide one, which can be tricky
    // with non-nullable properties in the primary constructor that don't have defaults.
    // Example (if no plugin and assuming User could be nullable for this constructor):
    // constructor() : this(user = User(), token = "default_token_placeholder", expiryDate = Instant.now()) {
    //     // This default User() would need to be valid or User would need to be nullable
    // }
    // The plugin is the much cleaner solution.
}