package com.rj.ecommerce.api.shared.messaging.email

import com.rj.ecommerce.api.shared.enums.EmailStatus // Assuming this is the correct Kotlin enum
import java.time.LocalDateTime
import java.util.UUID

data class EmailDeliveryStatusDTO(
    val messageId: String,
    val version: String,
    val originalMessageId: String? = null, // Nullable if not always present
    val status: EmailStatus,
    val recipientEmail: String? = null, // Nullable
    val errorMessage: String? = null,   // Nullable
    val additionalData: Map<String, Any> = emptyMap(),
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(messageId.isNotBlank()) { "Message ID cannot be blank" }
        require(version.isNotBlank()) { "Version cannot be blank" }
        // originalMessageId, recipientEmail, errorMessage are nullable so no blank check needed here unless specifically required not to be blank if present.
    }

    companion object {
        @JvmStatic
        fun success(originalMessageId: String, recipientEmail: String): EmailDeliveryStatusDTO {
            return EmailDeliveryStatusDTO(
                messageId = UUID.randomUUID().toString(),
                version = "1.0",
                originalMessageId = originalMessageId,
                status = EmailStatus.SENT, // Or DELIVERED depending on definition of success
                recipientEmail = recipientEmail,
                timestamp = LocalDateTime.now()
            )
        }

        @JvmStatic
        fun failure(originalMessageId: String?, recipientEmail: String?, errorMessage: String?): EmailDeliveryStatusDTO {
            return EmailDeliveryStatusDTO(
                messageId = UUID.randomUUID().toString(),
                version = "1.0",
                originalMessageId = originalMessageId,
                status = EmailStatus.FAILED,
                recipientEmail = recipientEmail,
                errorMessage = errorMessage,
                timestamp = LocalDateTime.now()
            )
        }

        // Default builder-like factory
        @JvmStatic
        fun build(
            status: EmailStatus,
            originalMessageId: String? = null,
            recipientEmail: String? = null,
            errorMessage: String? = null,
            additionalData: Map<String, Any> = emptyMap()
        ): EmailDeliveryStatusDTO {
            return EmailDeliveryStatusDTO(
                messageId = UUID.randomUUID().toString(),
                version = "1.0",
                originalMessageId = originalMessageId,
                status = status,
                recipientEmail = recipientEmail,
                errorMessage = errorMessage,
                additionalData = additionalData,
                timestamp = LocalDateTime.now()
            )
        }
    }
}