package com.rj.ecommerce_backend.product.domain

import com.rj.ecommerce_backend.product.valueobject.ProductDescription
import com.rj.ecommerce_backend.product.valueobject.ProductName
import com.rj.ecommerce_backend.product.valueobject.ProductPrice
import com.rj.ecommerce_backend.product.valueobject.StockQuantity
import jakarta.persistence.*
import lombok.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
@EntityListeners(AuditingEntityListener::class)
class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "name"))
    var productName: ProductName? = null

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "description"))
    var productDescription: ProductDescription? = null

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "price"))
    var productPrice: ProductPrice? = null

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "quantity"))
    var stockQuantity: StockQuantity? = null

    @Builder.Default
    @ManyToMany
    @JoinTable(
        name = "product_category",
        joinColumns = [JoinColumn(name = "product_id")],
        inverseJoinColumns = [JoinColumn(name = "category_id")]
    )
    var categories: MutableList<Category?> = ArrayList<Category?>()

    @OneToMany(
        mappedBy = "product",
        cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH],
        orphanRemoval = true
    )
    var imageList: MutableList<Image?> = ArrayList<Image?>()

    @CreationTimestamp
    private var createdAt: LocalDateTime? = null

    @UpdateTimestamp
    private var updatedAt: LocalDateTime? = null

    @CreatedBy
    private var createdBy: String? = null

    @LastModifiedBy
    private var lastModifiedBy: String? = null
}
