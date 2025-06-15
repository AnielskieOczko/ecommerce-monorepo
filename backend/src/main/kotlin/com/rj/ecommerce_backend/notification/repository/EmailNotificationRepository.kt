package com.rj.ecommerce_backend.notification.repository

import com.rj.ecommerce.api.shared.enums.NotificationDispatchStatus
import com.rj.ecommerce.api.shared.enums.NotificationEntityType
import com.rj.ecommerce_backend.notification.EmailNotification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EmailNotificationRepository: JpaRepository<EmailNotification, Long> {
    fun findByMessageId(messageId: String): EmailNotification?

    fun findByEntityTypeAndEntityId(entityType: NotificationEntityType, entityId: String):
            List<EmailNotification>
    fun findByRecipientOrderByCreatedAtDesc(recipient: String):
            List<EmailNotification>
    fun findByStatus(status: NotificationDispatchStatus): List<EmailNotification>
}