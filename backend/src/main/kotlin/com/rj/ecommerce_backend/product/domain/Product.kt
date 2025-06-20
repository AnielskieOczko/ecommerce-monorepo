package com.rj.ecommerce_backend.product.domain

import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.core.ProductDescription
import com.rj.ecommerce.api.shared.core.ProductName
import com.rj.ecommerce.api.shared.core.QuantityInStock
import jakarta.persistence.*
import jakarta.validation.Valid
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener::class)
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "name", nullable = false))
    @field:Valid // Cascade validation to ProductName VO
    var name: ProductName,

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "description"))
    @field:Valid
    var description: ProductDescription? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "price_amount", precision = 19, scale = 2, nullable = false)),
        AttributeOverride(name = "currencyCode", column = Column(name = "price_currency", length = 3, nullable = false))
    )
    @field:Valid // Cascade validation to Money VO
    var unitPrice: Money,

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "quantity_in_stock", nullable = false))
    @field:Valid
    var quantityInStock: QuantityInStock,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "product_category",
        joinColumns = [JoinColumn(name = "product_id")],
        inverseJoinColumns = [JoinColumn(name = "category_id")]
    )
    @BatchSize(size = 20)
    val categories: MutableSet<Category> = mutableSetOf(),

    @OneToMany(
        mappedBy = "product",
        cascade = [CascadeType.ALL], // CascadeType.ALL includes PERSIST, MERGE, REFRESH, REMOVE
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @BatchSize(size = 20)
    val images: MutableList<Image> = mutableListOf(),
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

    // Helper methods for bidirectional management of collections
    fun addCategory(category: Category) {
        categories.add(category)
        category.products.add(this) // Assuming Category has a 'products' collection
    }

    fun removeCategory(category: Category) {
        categories.remove(category)
        category.products.remove(this)
    }

    fun addImage(image: Image) {
        images.add(image)
        image.product = this
    }

    fun removeImage(image: Image) {
        images.remove(image)
        image.product = null
    }

    // If using data class, default equals/hashCode will include collections.
    // This can be problematic. Consider overriding if needed, e.g., based on ID.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Product
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: javaClass.hashCode()

    override fun toString(): String {
        return "Product(id=$id, productName=${name.value}, stockQuantity=${quantityInStock.value})"
    }
}
