package com.rj.ecommerce_backend.messaging.email.client

import com.rj.ecommerce.api.shared.enums.NotificationEntityType
import com.rj.ecommerce.api.shared.messaging.email.EcommerceEmailRequest
import com.rj.ecommerce.api.shared.messaging.email.NotificationCreationRequestDTO
import com.rj.ecommerce.api.shared.messaging.email.OrderEmailRequestDTO
import com.rj.ecommerce.api.shared.messaging.email.PaymentEmailRequestDTO
import com.rj.ecommerce.api.shared.messaging.email.WelcomeEmailRequestDTO
import com.rj.ecommerce_backend.messaging.common.exception.MessageDispatchException
import com.rj.ecommerce_backend.messaging.email.producer.EmailMessageProducer
import com.rj.ecommerce_backend.notification.service.EmailNotificationService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class EmailServiceClient(
    private val emailNotificationService: EmailNotificationService,
    private val emailMessageProducer: EmailMessageProducer
) {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    fun sendEmailRequest(request: EcommerceEmailRequest) {
        log.info { "Processing email request ${request.messageId} to ${request.to} using template ${request.template.name}" }

        val (entityType, entityId) = extractNotificationMetadata(request)

        val creationRequest = NotificationCreationRequestDTO(
            messageId = request.messageId,
            recipient = request.to,
            subject = request.subject,
            template = request.template,
            entityType = entityType,
            entityId = entityId
        )
        val notification = emailNotificationService.createNotification(creationRequest)

        try {
            emailMessageProducer.sendEmail(request, notification.messageId)
            log.info { "Successfully dispatched email request ${notification.messageId} to producer." }
        } catch (e: Exception) {
            val reason = "Failed to dispatch to message queue: ${e.message ?: "Unknown producer error"}"
            log.error(e) { "Error dispatching email ${notification.messageId}. Marking as FAILED. Reason: $reason" }
            emailNotificationService.markAsFailed(notification.messageId, reason)

            // Throw a new, more specific exception that wraps the original cause
            throw MessageDispatchException("Failed to send email request for messageId ${notification.messageId}", e)
        }
    }

    private fun extractNotificationMetadata(request: EcommerceEmailRequest): Pair<NotificationEntityType, String> {
        return when (request) {
            is OrderEmailRequestDTO -> NotificationEntityType.ORDER to request.orderId
            is PaymentEmailRequestDTO -> NotificationEntityType.PAYMENT to request.orderId.toString()
            is WelcomeEmailRequestDTO -> NotificationEntityType.CUSTOMER to request.to
            else -> NotificationEntityType.UNKNOWN to request.messageId
        }
    }
}