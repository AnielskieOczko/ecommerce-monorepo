package com.rj.ecommerce_backend.user.domain // Assuming this is the target package for domain entities

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "authorities")
@EntityListeners(AuditingEntityListener::class)
class Authority(
    @Column(nullable = false, unique = true)
    var name: String,

    // Initialize collections directly. 'val' as the reference to the set won't change.
    // The set should contain non-nullable User entities.
    @ManyToMany(mappedBy = "authorities", fetch = FetchType.LAZY) // Added LAZY fetch
    val users: MutableSet<User> = mutableSetOf()

) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null // Nullable because it's generated

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

    fun addUser(user: User) {
        users.add(user)
    }

    fun removeUser(user: User) {
        users.remove(user)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Authority) return false

        return if (id != null && id != 0L && other.id != null && other.id != 0L) {
            id == other.id
        } else {
            name == other.name
        }
    }

    override fun hashCode(): Int {
        return if (id != null && id != 0L) {
            id.hashCode()
        } else {
            name.hashCode()
        }
    }

    override fun toString(): String {
        return "Authority(id=$id, name='$name')"
    }
}