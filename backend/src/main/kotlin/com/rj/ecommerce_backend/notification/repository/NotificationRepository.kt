package com.rj.ecommerce_backend.notification.repository

import com.rj.ecommerce.api.shared.enums.NotificationDispatchStatus
import com.rj.ecommerce.api.shared.enums.NotificationEntityType
import com.rj.ecommerce_backend.notification.domain.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : JpaRepository<Notification, Long> {

    fun findByCorrelationId(correlationId: String): Notification?

    fun findByMessageId(messageId: String): Notification?

    fun findByEntityTypeAndEntityId(entityType: NotificationEntityType, entityId: String):
            List<Notification>

    fun findByRecipientOrderByCreatedAtDesc(recipient: String):
            List<Notification>

    fun findByStatus(status: NotificationDispatchStatus): List<Notification>
}