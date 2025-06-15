package com.rj.ecommerce_backend.notification

import com.rj.ecommerce.api.shared.enums.EmailTemplate
import com.rj.ecommerce.api.shared.enums.NotificationEntityType
import com.rj.ecommerce.api.shared.enums.NotificationStatus
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "email_notifications")
class EmailNotification(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "message_id", nullable = false, unique = true)
    val messageId: String,

    @Column(name = "recipient", nullable = false)
    val recipient: String,

    @Column(name = "subject", nullable = false)
    val subject: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "template", nullable = false)
    val template: EmailTemplate,

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    val entityType: NotificationEntityType,

    @Column(name = "entity_id", nullable = false)
    val entityId: String,

    // --- State properties ---

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: NotificationStatus = NotificationStatus.PENDING, // Default to PENDING

    @Column(name = "error_message", length = 1000)
    var errorMessage: String? = null,

    // --- Audit properties (now part of the primary constructor) ---

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null,

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    var createdBy: String? = null,

    @LastModifiedBy
    @Column(name = "last_modified_by")
    var lastModifiedBy: String? = null

) {
    // Companion object for factory functions, constants, etc.
    companion object {
        /**
         * A factory function to create a new notification in its initial PENDING state.
         */
        fun create(
            messageId: String,
            recipient: String,
            subject: String,
            template: EmailTemplate,
            entityType: NotificationEntityType,
            entityId: String
        ): EmailNotification {
            return EmailNotification(
                messageId = messageId,
                recipient = recipient,
                subject = subject,
                template = template,
                entityType = entityType,
                entityId = entityId
                // status defaults to PENDING automatically
            )
        }
    }
}

class NotificationNotFoundException(message: String) : RuntimeException(message)