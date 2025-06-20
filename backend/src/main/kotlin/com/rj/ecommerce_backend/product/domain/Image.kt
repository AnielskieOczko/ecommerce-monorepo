package com.rj.ecommerce_backend.product.domain

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "images",
    // The index name should be updated to reflect the new column name
    indexes = [Index(name = "idx_image_identifier", columnList = "fileIdentifier")]
)
@EntityListeners(AuditingEntityListener::class)
data class Image(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    // 1. RENAMED: 'path' is now 'fileIdentifier' for architectural consistency.
    @Column(nullable = false, unique = true)
    val fileIdentifier: String,

    @Column(unique = true)
    var webpFileIdentifier: String? = null,

    // 2. FIXED: 'altText' is now non-nullable to enforce data integrity.
    @Column(nullable = false)
    var altText: String,

    // 3. IMPROVED: 'mimeType' is now a 'val' and explicitly non-nullable.
    @Column(nullable = false)
    val mimeType: String,

    // 3. IMPROVED: 'product' is now a 'val'.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    ) {
    // 3. IMPROVED: Auditing fields are now 'val' where appropriate.
    @CreatedDate
    @Column(nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime
        private set

    @LastModifiedDate
    @Column(nullable = false)
    lateinit var updatedAt: LocalDateTime
        private set

    @CreatedBy
    @Column(updatable = false)
    var createdBy: String? = null
        private set

    @LastModifiedBy
    var lastModifiedBy: String? = null
        private set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Image
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: javaClass.hashCode()

    // 4. UPDATED: toString now uses the new field name.
    override fun toString(): String {
        return "Image(id=$id, fileIdentifier='$fileIdentifier', productId=${product.id})"
    }
}