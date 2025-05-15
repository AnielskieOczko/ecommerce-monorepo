package com.rj.ecommerce_backend.user.domain

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.Email
import com.rj.ecommerce.api.shared.core.Password
import com.rj.ecommerce.api.shared.core.PhoneNumber
import com.rj.ecommerce_backend.cart.domain.Cart
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
class User(

    var firstName: String?,
    var lastName: String?,

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "email", unique = true, nullable = false))
    var email: Email,

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "password_hash", nullable = false))
    var password: Password,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "street", column = Column(name = "address_street")),
        AttributeOverride(name = "city", column = Column(name = "address_city")),
        AttributeOverride(name = "zipCode.value", column = Column(name = "address_zip_code")),
        AttributeOverride(name = "country", column = Column(name = "address_country"))
    )
    var address: Address? = null,

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "phone_number"))
    var phoneNumber: PhoneNumber? = null,

    @Column(name = "date_of_birth")
    var dateOfBirth: LocalDate? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true, // Default value

    // --- Collections ---
    // Initialize collections. 'val' as the reference to the set itself won't change.
    // The set should contain non-nullable Authority entities.
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "users_authorities",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "authority_id", referencedColumnName = "id")]
    )
    val authorities: MutableSet<Authority> = mutableSetOf()

) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null // Nullable as it's generated

    // CascadeType.ALL on OneToOne means if User is deleted, Cart is deleted.
    // If User is saved, Cart is saved/updated.
    // mappedBy = "user" means Cart entity has a 'user' field that owns the relationship.
    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var cart: Cart? = null
        // Custom setter to manage bidirectional relationship if Cart also holds a User reference
        set(value) {
            field = value
            value?.user = this // Ensure Cart.user is set back to this User instance
        }

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

    fun addAuthority(authority: Authority) {
        authorities.add(authority)
        authority.users.add(this)
    }

    fun removeAuthority(authority: Authority) {
        authorities.remove(authority)
        authority.users.remove(this)
    }

    // --- equals, hashCode, toString ---
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false

        return if (id != null && id != 0L && other.id != null && other.id != 0L) {
            id == other.id
        } else {
            email.value == other.email.value
        }
    }

    override fun hashCode(): Int {
        return if (id != null && id != 0L) {
            id.hashCode()
        } else {
            email.value.hashCode()
        }
    }

    override fun toString(): String {
        // Avoid including collections (authorities) or potentially lazy-loaded OneToOne (cart)
        // in toString to prevent LazyInitializationException and verbosity.
        return "User(id=$id, firstName=$firstName, lastName=$lastName, email=${email.value}, isActive=$isActive)"
    }
}