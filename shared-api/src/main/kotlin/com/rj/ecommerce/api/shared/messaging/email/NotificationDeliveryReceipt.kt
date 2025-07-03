package com.rj.ecommerce.api.shared.messaging.email

import com.rj.ecommerce.api.shared.enums.NotificationChannel
import com.rj.ecommerce.api.shared.enums.NotificationDeliveryStatus
import com.rj.ecommerce.api.shared.messaging.contract.MessageVersioning
import java.time.LocalDateTime
import java.util.UUID

/**
 * A generic DTO representing a delivery receipt from any notification channel provider.
 */
data class NotificationDeliveryReceipt(
    val correlationId: String,
    val channel: NotificationChannel, // NEW: Specifies which channel this receipt is for.
    val status: NotificationDeliveryStatus, // CHANGED: Uses the new canonical enum.
    val recipientIdentifier: String, // RENAMED: Generic name for email, phone number, etc.
    val errorMessage: String? = null,
    val providerDetails: Map<String, Any> = emptyMap(), // For provider-specific data
    // Event's own metadata
    val eventId: String = UUID.randomUUID().toString(),
    val version: String = MessageVersioning.CURRENT_VERSION,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(correlationId.isNotBlank()) { "Correlation ID cannot be blank." }
        require(recipientIdentifier.isNotBlank()) { "Recipient identifier cannot be blank." }
        if (status == NotificationDeliveryStatus.FAILED) {
            require(!errorMessage.isNullOrBlank()) { "A FAILED receipt must include an error message." }
        }
    }
}