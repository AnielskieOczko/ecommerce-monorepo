package com.rj.ecommerce.api.shared.messaging.email


import com.rj.ecommerce.api.shared.enums.EmailDeliveryReceiptStatus
import com.rj.ecommerce.api.shared.messaging.contract.MessageVersioning.CURRENT_VERSION
import java.time.LocalDateTime
import java.util.UUID

/**
 * A DTO representing a delivery receipt event from an external email service.
 * This is an immutable record of an event that has occurred.
 */
data class EmailDeliveryReceiptDTO(
    val originalMessageId: String,
    val status: EmailDeliveryReceiptStatus,
    val recipientEmail: String,
    val errorMessage: String? = null, // Should only be present on BOUNCED or other failures
    val additionalData: Map<String, Any> = emptyMap(),
    // These fields are part of the event's own metadata
    val eventId: String = UUID.randomUUID().toString(),
    val version: String = CURRENT_VERSION,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    init {
        // Validation is still important
        require(originalMessageId.isNotBlank()) { "Original Message ID cannot be blank." }
        require(recipientEmail.isNotBlank()) { "Recipient email cannot be blank." }

        // A BOUNCED status should ideally have an error message
        if (status == EmailDeliveryReceiptStatus.BOUNCED) {
            require(!errorMessage.isNullOrBlank()) { "A bounced receipt must include an error message." }
        }
    }

    companion object {
        /**
         * A concise factory function for a successful delivery event.
         */
        @JvmStatic
        fun delivered(originalMessageId: String, recipientEmail: String): EmailDeliveryReceiptDTO {
            return EmailDeliveryReceiptDTO(
                originalMessageId = originalMessageId,
                status = EmailDeliveryReceiptStatus.DELIVERED,
                recipientEmail = recipientEmail
            )
        }

        /**
         * A concise factory function for a delivery failure (bounce) event.
         */
        @JvmStatic
        fun bounced(originalMessageId: String, recipientEmail: String, reason: String): EmailDeliveryReceiptDTO {
            return EmailDeliveryReceiptDTO(
                originalMessageId = originalMessageId,
                status = EmailDeliveryReceiptStatus.BOUNCED,
                recipientEmail = recipientEmail,
                errorMessage = reason
            )
        }
    }
}