package com.rj.ecommerce.api.shared.messaging.email


import com.rj.ecommerce.api.shared.enums.EmailDeliveryReceiptStatus
import com.rj.ecommerce.api.shared.messaging.contract.MessageVersioning
import java.time.LocalDateTime
import java.util.UUID

/**
 * A DTO representing a delivery receipt event from an external email service.
 * This is an immutable record of an event that has occurred.
 */
data class EmailDeliveryReceiptDTO(
    // RENAMED: This is the correlation ID of the original notification.
    val correlationId: String,
    val status: EmailDeliveryReceiptStatus,
    val recipientEmail: String,
    val errorMessage: String? = null,
    val additionalData: Map<String, Any> = emptyMap(),
    // Event's own metadata
    val eventId: String = UUID.randomUUID().toString(),
    val version: String = MessageVersioning.CURRENT_VERSION,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(correlationId.isNotBlank()) { "Correlation ID cannot be blank." }
        require(recipientEmail.isNotBlank()) { "Recipient email cannot be blank." }
        if (status == EmailDeliveryReceiptStatus.BOUNCED) {
            require(!errorMessage.isNullOrBlank()) { "A bounced receipt must include an error message." }
        }
    }
}