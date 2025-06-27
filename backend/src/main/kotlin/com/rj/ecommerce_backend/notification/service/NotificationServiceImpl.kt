package com.rj.ecommerce_backend.notification.service

import com.rj.ecommerce.api.shared.enums.EmailDeliveryReceiptStatus
import com.rj.ecommerce.api.shared.enums.NotificationDispatchStatus
import com.rj.ecommerce.api.shared.messaging.email.EmailDeliveryReceiptDTO
import com.rj.ecommerce_backend.notification.Notification
import com.rj.ecommerce_backend.notification.exception.NotificationDispatchException

import com.rj.ecommerce_backend.notification.repository.NotificationRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
class NotificationServiceImpl(
    private val notificationRepository: NotificationRepository,
    private val providerFactory: NotificationProviderFactory
) : NotificationService {

    @Transactional
    override fun dispatch(notification: Notification): Notification {
        log.info { "Dispatching notification for recipient: ${notification.recipient}, channel: ${notification.channel}, template: ${notification.template}" }

        try {
            // 1. Persist the notification first. This is critical.
            // It saves the initial PENDING state and generates the correlationId within a transaction.
            // If this fails, the whole operation rolls back and nothing is sent.
            val savedNotification = notificationRepository.save(notification)
            log.debug { "Notification persisted with ID: ${savedNotification.id} and Correlation ID: ${savedNotification.correlationId}" }

            // 2. Get the correct provider for the requested channel.
            val provider = providerFactory.getProvider(savedNotification.channel)

            // 3. Delegate the actual sending to the provider.
            // The provider now has the fully persisted notification object, including its ID and correlationId.
            provider.send(savedNotification)

            // 4. Update the status to SENT and save again.
            // This happens in the same transaction. If the provider.send() fails, this is rolled back.
            savedNotification.status = NotificationDispatchStatus.SENT
            notificationRepository.save(savedNotification)
            log.info { "Notification with Correlation ID: ${savedNotification.correlationId} successfully dispatched to provider." }

            return savedNotification

        } catch (e: Exception) {
            // If any part of the process fails (DB save, provider lookup, or provider send),
            // the transaction will be rolled back. We log the error and re-throw it.
            log.error(e) { "Failed to dispatch notification for recipient: ${notification.recipient}. Transaction will be rolled back." }
            // Optionally wrap in a custom exception
            throw NotificationDispatchException("Failed to dispatch notification", e)
        }
    }

    @Transactional
    override fun updateStatusFromReceipt(receipt: EmailDeliveryReceiptDTO) {
        val notification = findByCorrelationId(receipt.correlationId)
            ?: run {
                log.warn { "Received a delivery receipt for an unknown notification. Correlation ID: ${receipt.correlationId}" }
                // It's important to not throw an exception here that would cause the message to be re-queued indefinitely.
                // This is a terminal state for this message. We simply acknowledge and log it.
                return
            }

        when (receipt.status) {
            EmailDeliveryReceiptStatus.DELIVERED -> {
                notification.status = NotificationDispatchStatus.DELIVERED
                notification.errorMessage = null
                log.info { "Notification status updated to DELIVERED for correlationId: ${receipt.correlationId}" }
            }
            EmailDeliveryReceiptStatus.BOUNCED -> {
                notification.status = NotificationDispatchStatus.FAILED
                notification.errorMessage = receipt.errorMessage ?: "Delivery failed (bounced)."
                log.warn { "Notification status updated to FAILED for correlationId: ${receipt.correlationId}. Reason: ${notification.errorMessage}" }
            }
            else -> {
                log.info { "Received non-terminal status update '${receipt.status}' for correlationId: ${receipt.correlationId}. No state change." }
                return // No need to save if no state changed.
            }
        }
        notificationRepository.save(notification)
    }

    @Transactional(readOnly = true)
    override fun findByCorrelationId(correlationId: String): Notification? {
        return notificationRepository.findByCorrelationId(correlationId)
    }
}