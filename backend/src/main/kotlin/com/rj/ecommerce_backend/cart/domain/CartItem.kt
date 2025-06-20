package com.rj.ecommerce_backend.cart.domain

import com.rj.ecommerce_backend.product.domain.Product
import jakarta.persistence.*
import jakarta.validation.constraints.Min
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "cart_items")
@EntityListeners(AuditingEntityListener::class)
class CartItem(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    @Column(nullable = false)
    @field:Min(1, message = "Quantity must be at least 1")
    var quantity: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    var cart: Cart? = null

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
        if (this === other) return true
        if (other !is CartItem) return false

        return if (id != null && id != 0L && other.id != null && other.id != 0L) {
            id == other.id
        } else {
            (product.id != null && product.id == other.product.id) && (cart?.id != null && cart?.id == other.cart?.id)
        }
    }

    override fun hashCode(): Int {
        // 1. Check if the entity is likely persisted (has a non-null, non-zero ID)
        return if (id != null && id != 0L) {
            // 2. If persisted, base the hashCode solely on the 'id'.
            //    This is the most stable and unique identifier for a persisted entity.
            id.hashCode()
        } else {
            // 3. If the entity is transient (id is null or 0),
            //    base the hashCode on the same business key fields used in the 'equals()' method
            //    for transient objects: product.id and cart.id.

            // 3a. Start with the hashCode of product.id.
            //     'product.id?.hashCode()' uses the safe call operator. If product.id is null,
            //     it evaluates to null.
            //     '?: 0' (Elvis operator) provides a default value of 0 if product.id or its hashCode is null.
            //     Using 0 is a common practice for null fields in hashCode calculations.
            var result = product.id?.hashCode() ?: 0

            // 3b. Combine the hashCode of cart.id with the current 'result'.
            //     The number 31 is a common prime number used in hashCode calculations.
            //     Multiplying by a prime helps distribute hash codes more evenly and reduce collisions.
            //     'cart?.id?.hashCode() ?: 0' again handles potential nulls for cart or cart.id.
            result = 31 * result + (cart?.id?.hashCode() ?: 0)

            // 3c. Return the combined result.
            result
        }
    }

    override fun toString(): String {
        return "CartItem(id=$id, productId=${product.id}, quantity=$quantity, cartId=${cart?.id})"
    }


}