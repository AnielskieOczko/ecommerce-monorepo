package com.rj.ecommerce_backend.user

import com.rj.ecommerce_backend.cart.domain.Cart
import com.rj.ecommerce_backend.user.valueobject.Address
import com.rj.ecommerce_backend.user.valueobject.Email
import com.rj.ecommerce_backend.user.valueobject.Password
import com.rj.ecommerce_backend.user.valueobject.PhoneNumber
import jakarta.persistence.*
import lombok.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime

@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Long? = null

    private var firstName: String? = null
    private var lastName: String? = null

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "email", unique = true))
    private var email: Email? = null

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "password"))
    private var password: Password? = null

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    private var cart: Cart? = null

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "street", column = Column(name = "address_street")),
        AttributeOverride(name = "city", column = Column(name = "address_city")),
        AttributeOverride(name = "zipCode", column = Column(name = "address_zip_code")),
        AttributeOverride(name = "country", column = Column(name = "address_country"))
    )
    private var address: Address? = null

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "phone_number"))
    private var phoneNumber: PhoneNumber? = null

    @Column(name = "date_of_birth")
    var dateOfBirth: LocalDate? = null

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "users_authorities",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "authority_id")]
    )
    private val authorities: MutableSet<Authority?> = HashSet<Authority?>()

    @Column(name = "is_active", nullable = false)
    private var isActive = true

    @CreationTimestamp
    private var createdAt: LocalDateTime? = null

    @UpdateTimestamp
    private var updatedAt: LocalDateTime? = null

    @CreatedBy
    private var createdBy: String? = null

    @LastModifiedBy
    private var lastModifiedBy: String? = null
}

