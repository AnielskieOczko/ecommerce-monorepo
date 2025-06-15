package com.rj.ecommerce_backend.notification.service

import com.rj.ecommerce.api.shared.enums.NotificationDispatchStatus
import com.rj.ecommerce.api.shared.enums.NotificationEntityType
import com.rj.ecommerce.api.shared.messaging.email.NotificationCreationRequestDTO
import com.rj.ecommerce_backend.notification.EmailNotification
import com.rj.ecommerce_backend.notification.repository.EmailNotificationRepository
import com.rj.ecommerce_backend.notification.exception.NotificationNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class EmailNotificationServiceImpl(
    private val emailNotificationRepository: EmailNotificationRepository
) : EmailNotificationService {

    companion object {
        private val log = KotlinLogging.logger {}
    }

    override fun createNotification(request: NotificationCreationRequestDTO): EmailNotification {
        log.info { "Creating notification record for messageId: ${request.messageId}" }
        val newNotification = EmailNotification.Companion.create(
            messageId = request.messageId,
            recipient = request.recipient,
            subject = request.subject,
            template = request.template,
            entityType = request.entityType,
            entityId = request.entityId
        )
        return emailNotificationRepository.save(newNotification)
    }

    override fun markAsSent(messageId: String) {
        log.info { "Marking notification as SENT for messageId: $messageId" }
        val notification = findNotificationOrThrow(messageId)

        notification.status = NotificationDispatchStatus.SENT
        notification.errorMessage = null

        emailNotificationRepository.save(notification)
    }

    override fun markAsFailed(messageId: String, reason: String) {
        log.warn { "Marking notification as FAILED for messageId: $messageId, reason: $reason" }
        val notification = findNotificationOrThrow(messageId)

        notification.status = NotificationDispatchStatus.FAILED
        notification.errorMessage = reason

        emailNotificationRepository.save(notification)
    }

    override fun getEmailNotificationsForEntity(entityType: NotificationEntityType, entityId: String): List<EmailNotification> {
        return emailNotificationRepository.findByEntityTypeAndEntityId(entityType, entityId)
    }

    override fun getEmailNotificationsForRecipient(recipient: String): List<EmailNotification> {
        return emailNotificationRepository.findByRecipientOrderByCreatedAtDesc(recipient)
    }

    override fun getEmailNotificationsByStatus(status: NotificationDispatchStatus): List<EmailNotification> {
        return emailNotificationRepository.findByStatus(status)
    }

    /**
     * Private helper to find a notification or throw a specific exception.
     * This avoids code duplication and silent failures.
     */
    private fun findNotificationOrThrow(messageId: String): EmailNotification {
        return emailNotificationRepository.findByMessageId(messageId)
            ?: throw NotificationNotFoundException("No email notification found with messageId: $messageId")
    }
}