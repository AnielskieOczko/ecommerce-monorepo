package com.rj.ecommerce_backend.product.domain

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime



@Entity
@Table(name = "images")
@EntityListeners(AuditingEntityListener::class)
// It's often better not to use a data class if you have custom equals/hashCode/toString
// and mutable properties, to avoid confusion. A regular class is clearer here.
class Image(
    // Properties required at creation time go into the primary constructor.
    @Column(nullable = false, unique = true)
    val fileIdentifier: String,

    @Column(nullable = false)
    var altText: String,

    @Column(nullable = false)
    val mimeType: String,

    // The "many-to-one" side of the relationship.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(unique = true)
    var webpFileIdentifier: String? = null

    // --- Auditing fields ---
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

    // Internal method for the owning side (Product) to manage the relationship
    internal fun setProduct(newProduct: Product?) {
        this.product = newProduct ?: throw IllegalArgumentException("Product cannot be set to null on an existing image.")
    }

    internal fun clearProduct() {
        // This function is only for JPA's orphanRemoval.
        // We don't expect a product to be nullable, but this allows the relationship to be severed before deletion.
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Image
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: javaClass.hashCode()

    override fun toString(): String {
        return "Image(id=$id, fileIdentifier='$fileIdentifier', productId=${product.id})"
    }
}