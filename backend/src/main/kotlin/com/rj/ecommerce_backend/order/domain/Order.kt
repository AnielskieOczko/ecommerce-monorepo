package com.rj.ecommerce_backend.order.domain

import com.rj.ecommerce_backend.api.shared.core.Address
import com.rj.ecommerce_backend.api.shared.enums.CanonicalPaymentStatus
import com.rj.ecommerce_backend.api.shared.enums.Currency
import com.rj.ecommerce_backend.api.shared.enums.OrderStatus
import com.rj.ecommerce_backend.api.shared.enums.PaymentMethod
import com.rj.ecommerce_backend.api.shared.enums.ShippingMethod
import com.rj.ecommerce_backend.user.domain.User
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener::class)
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY) // Consider LAZY fetching
    @JoinColumn(name = "user_id")
    var user: User? = null,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    val orderItems: MutableList<OrderItem> = mutableListOf(),

    @Column(precision = 19, scale = 2)
    var totalAmount: BigDecimal? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var currency: Currency = Currency.PLN,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "street", column = Column(name = "address_street")),
        AttributeOverride(name = "city", column = Column(name = "address_city")),
        AttributeOverride(name = "zipCode.value", column = Column(name = "address_zip_code")), // Ensure Address.zipCode has a 'value' field or adjust
        AttributeOverride(name = "country", column = Column(name = "address_country"))
    )
    var shippingAddress: Address? = null,

    @Enumerated(EnumType.STRING)
    var shippingMethod: ShippingMethod? = null,

    @Enumerated(EnumType.STRING)
    var paymentMethod: PaymentMethod? = null,

    @Column(nullable = true)
    var paymentTransactionId: String? = null,

    @Column(nullable = true, length = 1024)
    var checkoutSessionUrl: String? = null,

    @Column(nullable = true)
    var checkoutSessionExpiresAt: LocalDateTime? = null,

    @Column(nullable = true, length = 1024)
    var receiptUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var paymentStatus: CanonicalPaymentStatus = CanonicalPaymentStatus.PENDING,

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    var orderStatus: OrderStatus = OrderStatus.PENDING,

    // Auditing fields are better placed in the body
    // if not strictly part of the primary data for object construction
) {
    @CreationTimestamp
    @Column(nullable = false, updatable = false) // orderDate is essentially a creation timestamp for the order itself
    var orderDate: LocalDateTime? = null

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

    // Helper function to manage bidirectional relationship if needed
    fun addOrderItem(orderItem: OrderItem) {
        orderItems.add(orderItem)
        orderItem.order = this
    }

    fun removeOrderItem(orderItem: OrderItem) {
        orderItems.remove(orderItem)
        orderItem.order = null
    }

    // Consider overriding toString, equals, and hashCode if orderItems can cause issues
    // (e.g., lazy loading exceptions, performance with large lists, or circular dependencies).
    // For data classes, properties in the primary constructor are used by default.
    // If orderItems is large or causes issues, you might exclude it:
    override fun toString(): String {
        return "Order(id=$id, userId=${user?.id}, totalPrice=$totalAmount, currency=$currency, orderStatus=$orderStatus)"
    }

    // Default equals/hashCode from data class will include orderItems.
    // If this is not desired (e.g., for performance or because only ID defines equality),
    // you'd need to override them as well. For many JPA entities, equality based on ID is common
    // after persistence.

    // Example of equals/hashCode based on ID (if non-null):
    // override fun equals(other: Any?): Boolean {
    //     if (this === other) return true
    //     if (javaClass != other?.javaClass) return false
    //     other as Order
    //     return id != null && id == other.id
    // }

    // override fun hashCode(): Int {
    //     return id?.hashCode() ?: javaClass.hashCode()
    // }
}