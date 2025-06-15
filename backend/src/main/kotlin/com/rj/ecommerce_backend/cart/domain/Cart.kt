package com.rj.ecommerce_backend.cart.domain

import com.rj.ecommerce_backend.order.domain.OrderItem
import com.rj.ecommerce_backend.user.domain.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime


@Entity
@Table(name = "carts")
@EntityListeners(AuditingEntityListener::class)
class Cart(

    @OneToOne
    @JoinColumn(name = "userId", unique = true, nullable = false)
    var user: User? = null,


    ) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @OneToMany(
        mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY
    )
    @BatchSize(size = 20)
    val cartItems: MutableList<CartItem> = mutableListOf()

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


    fun addCartItem(cartItem: CartItem) {
        cartItems.add(cartItem)
        cartItem.cart = this
    }

    fun removeCartItem(cartItem: CartItem) {
        val removed = cartItems.remove(cartItem)
        if (removed) cartItem.cart = null
    }

    fun clearCart() {
        val itemsToRemove = ArrayList(cartItems)
        itemsToRemove.forEach(
            action = { item -> removeCartItem(item) }
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this == other) return true
        if (other !is Cart) return false

        return if (this.id != null && other.id != null) {
            id == other.id
        } else {
            return false
        }
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: javaClass.name.hashCode()
    }

    override fun toString(): String {
        // Avoid including 'cartItems' or 'user' directly if lazy-loaded to prevent issues.
        return "Cart(id=$id, userId=${user?.id}, itemCount=${cartItems.size})"
    }
}