package com.rj.ecommerce_backend.product.domain

import jakarta.persistence.*
import lombok.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "categories")
@EntityListeners(AuditingEntityListener::class)
data class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var name: String,

    // For ManyToMany with Product
    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    val products: MutableSet<Product> = mutableSetOf(),
) {
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

    // Consider overriding equals/hashCode based on ID or natural key (name)
    // Default data class equals/hashCode will include 'products' collection, which is usually bad.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Category
        // Prefer ID-based equality after persistence, or natural key if ID is null
        return if (id != null) id == other.id else name != null && name == other.name
    }

    override fun hashCode(): Int = id?.hashCode() ?: name?.hashCode() ?: javaClass.hashCode()

    override fun toString(): String {
        return "Category(id=$id, name=$name)"
    }
}
