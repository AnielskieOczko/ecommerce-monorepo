package com.rj.ecommerce_backend.order.domain

import com.rj.ecommerce_backend.product.domain.Product
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
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener::class)
class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Long? = null

    @ManyToOne
    @JoinColumn(name = "order_id")
    private var order: Order? = null

    @ManyToOne
    @JoinColumn(name = "product_id")
    private var product: Product? = null

    private var quantity = 0

    private var price: BigDecimal? = null

    @CreationTimestamp
    private var createdAt: LocalDateTime? = null

    @UpdateTimestamp
    private var updatedAt: LocalDateTime? = null

    @CreatedBy
    private var createdBy: String? = null

    @LastModifiedBy
    private var lastModifiedBy: String? = null
}
