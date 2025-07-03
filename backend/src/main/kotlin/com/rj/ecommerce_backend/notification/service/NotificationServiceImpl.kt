package com.rj.ecommerce_backend.notification.service

import com.rj.ecommerce.api.shared.enums.EmailDeliveryReceiptStatus
import com.rj.ecommerce.api.shared.enums.NotificationDeliveryStatus
import com.rj.ecommerce.api.shared.enums.NotificationDispatchStatus
import com.rj.ecommerce_backend.notification.command.CreateNotificationCommand
import com.rj.ecommerce.api.shared.messaging.email.NotificationDeliveryReceipt
import com.rj.ecommerce.api.shared.messaging.email.NotificationEnvelope
import com.rj.ecommerce.api.shared.messaging.email.NotificationRequest
import com.rj.ecommerce_backend.messaging.email.producer.NotificationMessageProducer
import com.rj.ecommerce_backend.notification.domain.Notification
import com.rj.ecommerce_backend.notification.context.NotificationContext
import com.rj.ecommerce_backend.notification.mapper.OrderNotificationMapper

import com.rj.ecommerce_backend.notification.repository.NotificationRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
class NotificationServiceImpl(
    private val notificationRepository: NotificationRepository,
    private val messageProducer: NotificationMessageProducer,
    private val orderMapper: OrderNotificationMapper // Assuming it's needed for payload creation
) : NotificationService {

    @Transactional
    override fun dispatch(command: CreateNotificationCommand): List<Notification> {
        log.info { "Preparing notification for entity: ${command.entityType}/${command.entityId} to channels: ${command.channels}" }

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
        log.debug { "Persisted ${notifications.size} notification records." }

        // 2. Create the single, multi-channel message to send to the notification-service.
        // The correlationId can be taken from the first notification record, as it's shared for this event.
        val correlationId = notifications.first().correlationId

        // The payload is created once from the context.
        val payload = createPayloadFromContext(command.context)

        val envelope = NotificationEnvelope(
            to = command.recipient,
            subject = command.subject,
            template = command.template,
            channels = command.channels,
            correlationId = correlationId
        )

        val request = NotificationRequest(envelope, payload)

        // 3. Send the single message to RabbitMQ.
        try {
            messageProducer.send(request)
            // 4. Update status to SENT for all persisted records.
            notifications.forEach { it.status = com.rj.ecommerce.api.shared.enums.NotificationDispatchStatus.SENT }
            notificationRepository.saveAll(notifications)
            log.info { "Successfully dispatched multi-channel request with Correlation ID: $correlationId" }
        } catch (e: Exception) {
            log.error(e) { "Failed to dispatch multi-channel request with Correlation ID: $correlationId. Transaction will be rolled back." }
            throw e // Re-throw to trigger transaction rollback
        }

        return notifications
    }

    private fun createPayloadFromContext(context: NotificationContext): Any {
        return when (context) {
            is NotificationContext.OrderContext -> orderMapper.createOrderPayload(context.order)
            is NotificationContext.OrderStatusUpdateContext -> {
                // This mapping logic should also be in a dedicated mapper.
                // For now, we do it here for simplicity.
                val order = context.order
                com.rj.ecommerce.api.shared.messaging.email.payload.OrderStatusUpdatePayload(
                    orderId = order.id.toString(),
                    orderNumber = order.id.toString(),
                    customerName = order.user?.firstName,
                    newStatus = order.orderStatus,
                    previousStatus = context.previousStatus
                )
            }
            is NotificationContext.EmptyContext -> Unit // Return Unit for empty payload
        }
    }

    @Transactional
    override fun updateStatusFromReceipt(receipt: NotificationDeliveryReceipt) {
        // 1. Find the original notification using the correlation ID from the receipt.
        val notification = findByCorrelationId(receipt.correlationId)
            ?: run {
                // This is not an error we should retry. The message is either invalid or has arrived
                // after the original record was purged. We log it and acknowledge the message.
                log.warn { "Received a delivery receipt for an unknown notification. Discarding message. Correlation ID: ${receipt.correlationId}" }
                return
            }

        // 2. Perform a sanity check to ensure the receipt is for the correct channel.
        if (notification.channel != receipt.channel) {
            log.error {
                "CRITICAL: Mismatched channel on delivery receipt! " +
                        "Notification (ID: ${notification.id}) was for channel ${notification.channel}, " +
                        "but receipt reported channel ${receipt.channel}. " +
                        "CorrelationId: ${receipt.correlationId}"
            }
            // Do not process a mismatched receipt.
            return
        }

        // 3. Avoid processing if the notification is already in a terminal state.
        if (notification.status == NotificationDispatchStatus.DELIVERED || notification.status == NotificationDispatchStatus.FAILED) {
            log.info { "Ignoring duplicate receipt for already-terminal notification. CorrelationId: ${receipt.correlationId}, Current Status: ${notification.status}" }
            return
        }

        // 4. Update the notification's state based on the receipt's status.
        when (receipt.status) {
            NotificationDeliveryStatus.DELIVERED -> {
                notification.status = NotificationDispatchStatus.DELIVERED
                notification.errorMessage = null // Clear any previous transient errors
                log.info { "Notification status updated to DELIVERED for correlationId: ${receipt.correlationId}" }
            }
            NotificationDeliveryStatus.FAILED -> {
                notification.status = NotificationDispatchStatus.FAILED
                notification.errorMessage = receipt.errorMessage ?: "Delivery failed with no reason provided."
                log.warn { "Notification status updated to FAILED for correlationId: ${receipt.correlationId}. Reason: ${notification.errorMessage}" }
            }
            NotificationDeliveryStatus.UNKNOWN -> {
                // We received a status we don't understand. Log it but don't change the state.
                // It remains as SENT, awaiting a definitive terminal status.
                log.warn { "Received UNKNOWN delivery status for correlationId: ${receipt.correlationId}. No state change will be applied." }
                return // Exit without saving.
            }
        }

        // 5. Persist the changes to the database.
        notificationRepository.save(notification)
    }

    @Transactional(readOnly = true)
    override fun findByCorrelationId(correlationId: String): Notification? {
        return notificationRepository.findByCorrelationId(correlationId)
    }
}