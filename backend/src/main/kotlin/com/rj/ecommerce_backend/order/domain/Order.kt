package order.domain

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.enums.OrderStatus
import com.rj.ecommerce.api.shared.enums.PaymentMethod
import com.rj.ecommerce.api.shared.enums.PaymentStatus
import com.rj.ecommerce.api.shared.enums.ShippingMethod
import com.rj.ecommerce_backend.order.enums.Currency
import com.rj.ecommerce_backend.user.domain.User
import jakarta.persistence.*
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Data
import lombok.NoArgsConstructor
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDateTime


@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener::class)
class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Long? = null

    @ManyToOne
    @JoinColumn(name = "user_id")
    private var user: User? = null

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val orderItems: MutableList<OrderItem?> = ArrayList<OrderItem?>()

    @Column(precision = 19, scale = 2)
    private var totalPrice: BigDecimal? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private var currency = Currency.PLN

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "street", column = Column(name = "address_street")),
        AttributeOverride(name = "city", column = Column(name = "address_city")),
        AttributeOverride(name = "zipCode.value", column = Column(name = "address_zip_code")),
        AttributeOverride(name = "country", column = Column(name = "address_country"))
    )
    private var shippingAddress: Address? = null

    @Enumerated(EnumType.STRING)
    private var shippingMethod: ShippingMethod? = null

    @Enumerated(EnumType.STRING)
    private var paymentMethod: PaymentMethod? = null

    @Column(nullable = true)
    private var paymentTransactionId: String? = null

    @Column(nullable = true, length = 1024)
    private var checkoutSessionUrl: String? = null

    @Column(nullable = true)
    private var checkoutSessionExpiresAt: LocalDateTime? = null

    @Column(nullable = true, length = 1024)
    private var receiptUrl: String? = null

    @Enumerated(EnumType.STRING)
    private var paymentStatus: PaymentStatus? = null

    @CreationTimestamp
    private var orderDate: LocalDateTime? = null

    @Enumerated(EnumType.STRING)
    private var orderStatus: OrderStatus? = null

    @CreationTimestamp
    private var createdAt: LocalDateTime? = null

    @UpdateTimestamp
    private var updatedAt: LocalDateTime? = null

    @CreatedBy
    private var createdBy: String? = null

    @LastModifiedBy
    private var lastModifiedBy: String? = null
}
