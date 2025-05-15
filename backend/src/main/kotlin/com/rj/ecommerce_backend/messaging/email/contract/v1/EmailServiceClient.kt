package com.rj.ecommerce_backend.messaging.email.contract.v1

import com.rj.ecommerce.api.shared.messaging.email.OrderEmailRequestDTO
import com.rj.ecommerce.api.shared.messaging.email.EcommerceEmailRequest
import com.rj.ecommerce.api.shared.messaging.email.PaymentEmailRequestDTO
import com.rj.ecommerce.api.shared.messaging.email.WelcomeEmailRequestDTO
import com.rj.ecommerce_backend.messaging.email.producer.EmailMessageProducer
import com.rj.ecommerce_backend.notification.EmailNotificationService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class EmailServiceClient( // Constructor injection
    private val emailNotificationService: EmailNotificationService,
    private val emailMessageProducer: EmailMessageProducer
) {

    /**
     * Sends an email request, records it, and dispatches it via the producer.
     */
    fun sendEmailRequest(request: EcommerceEmailRequest) { // Parameter uses the interface
        logger.info { "Sending email request: ${request.messageId} to ${request.to} using template ${request.template.name}" }

        // Record the email being sent
        val entityType = determineEntityType(request)
        val entityId = extractEntityId(request)

        emailNotificationService.recordEmailSent(
            request.messageId,
            request.to,
            request.subject,
            request.template.name,
            entityType,
            entityId
        )

        // Send to message queue or actual email sending mechanism
        emailMessageProducer.sendEmail(request, request.messageId) // Assuming this signature

        logger.info { "Email request with ID ${request.messageId} processed and sent to producer." }
    }

    private fun determineEntityType(request: EcommerceEmailRequest): String {
        return when (request) {
            is OrderEmailRequestDTO -> "ORDER"
            is WelcomeEmailRequestDTO -> "CUSTOMER_WELCOME"
            is PaymentEmailRequestDTO -> "PAYMENT"

            else -> "UNKNOWN"
        }
    }

    private fun extractEntityId(request: EcommerceEmailRequest): String? {
        return when (request) {
            // After 'is OrderEmailRequestDTO', 'request' is smart-cast to OrderEmailRequestDTO
            is OrderEmailRequestDTO -> request.orderId
            // Example: If PaymentEmailRequestDTO also has an orderId relevant as the entityId
            is PaymentEmailRequestDTO -> request.orderId
            // Example: If WelcomeEmailRequestDTO doesn't have a relevant single entity ID for this purpose
            // is WelcomeEmailRequestDTO -> null // Or some other identifier if applicable
            // Add more 'is YourSpecificDtoType -> request.someRelevantIdProperty' checks here
            else -> null
        }
    }
}