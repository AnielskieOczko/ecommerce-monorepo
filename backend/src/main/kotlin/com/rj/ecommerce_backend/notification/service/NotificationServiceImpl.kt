package com.rj.ecommerce_backend.notification.service

import com.rj.ecommerce_backend.api.shared.enums.NotificationDispatchStatus
import com.rj.ecommerce_backend.notification.command.CreateNotificationCommand
import com.rj.ecommerce_backend.notification.domain.Notification
import com.rj.ecommerce_backend.notification.repository.NotificationRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
class NotificationServiceImpl(
    private val notificationRepository: NotificationRepository,
    private val notificationOrchestrator: NotificationOrchestrator
) : NotificationService {

    @Transactional
    override fun dispatch(command: CreateNotificationCommand): List<Notification> {
        log.info { "Dispatching notification for entity: ${command.entityType}/${command.entityId} to channels: ${command.channels}" }

        // 1. Persist a record for each requested channel dispatch.
        val notifications = command.channels.map { channel ->
            Notification(
                recipient = command.recipient,
                subject = command.subject,
                entityType = command.entityType,
                entityId = command.entityId,
                channel = channel,
                template = command.template,
                context = command.context
            )
        }
        notificationRepository.saveAll(notifications)

        try {
            // The orchestrator is now called in the same transaction.
            notificationOrchestrator.process(command)

            // 3. Update status to SENT.
            notifications.forEach { it.status = NotificationDispatchStatus.SENT }
            log.info { "Successfully processed and dispatched notification with Correlation ID: ${notifications.first().correlationId}" }

        } catch (e: Exception) {
            log.error(e) { "Failed to dispatch notification. Marking as FAILED." }
            notifications.forEach {
                it.status = NotificationDispatchStatus.FAILED
                it.errorMessage = e.message
            }
        }

        notificationRepository.saveAll(notifications)
        log.debug { "Persisted ${notifications.size} notification records." }

        return notifications
    }

    @Transactional(readOnly = true)
    override fun findByCorrelationId(correlationId: String): Notification? {
        return notificationRepository.findByCorrelationId(correlationId)
    }
}