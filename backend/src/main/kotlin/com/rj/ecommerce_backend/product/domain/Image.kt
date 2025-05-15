package com.rj.ecommerce_backend.product.domain

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
import java.time.LocalDateTime

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener::class)
class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Long? = null

    private var path: String? = null
    private var altText: String? = null
    private var mimeType: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private var product: Product? = null


    @CreationTimestamp
    private var createdAt: LocalDateTime? = null

    @UpdateTimestamp
    private var updatedAt: LocalDateTime? = null

    @CreatedBy
    private var createdBy: String? = null

    @LastModifiedBy
    private var lastModifiedBy: String? = null
}
