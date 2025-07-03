package com.rj.ecommerce_backend.notification.domain

import com.rj.ecommerce.api.shared.enums.NotificationChannel
import com.rj.ecommerce.api.shared.enums.NotificationDispatchStatus
import com.rj.ecommerce.api.shared.enums.NotificationEntityType
import com.rj.ecommerce.api.shared.enums.NotificationTemplate
import com.rj.ecommerce_backend.notification.context.NotificationContext
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.UUID

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "notifications")
// Using a data class is often ideal for entities, giving you equals, hashCode, etc. for free.
// All properties are now in the primary constructor.
data class Notification(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    // --- Required fields for creation ---
    val recipient: String,
    val subject: String,
    val entityType: NotificationEntityType,
    val entityId: String,

    @Enumerated(EnumType.STRING)
    val channel: NotificationChannel,

    @Enumerated(EnumType.STRING)
    val template: NotificationTemplate,

    // --- Fields with sensible defaults ---
    @Column(unique = true, nullable = false)
    val correlationId: String = UUID.randomUUID().toString(),

    @Enumerated(EnumType.STRING)
    var status: NotificationDispatchStatus = NotificationDispatchStatus.PENDING,

    @Column(length = 1000)
    var errorMessage: String? = null,

    // --- Transient context field with a default value ---
    @Transient
    val context: NotificationContext = NotificationContext.EmptyContext,

    // --- Auditing fields (managed by Spring Data JPA, should be var) ---
    @CreationTimestamp
    @Column(updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    var updatedAt: LocalDateTime? = null,

    @CreatedBy
    @Column(updatable = false)
    var createdBy: String? = null,

    @LastModifiedBy
    var lastModifiedBy: String? = null
)