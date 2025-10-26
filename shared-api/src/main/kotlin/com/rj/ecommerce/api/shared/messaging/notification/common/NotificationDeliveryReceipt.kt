package com.rj.ecommerce.api.shared.messaging.notification.common

import com.rj.ecommerce.api.shared.enums.NotificationChannel
import com.rj.ecommerce.api.shared.enums.NotificationDeliveryStatus
import com.rj.ecommerce.api.shared.messaging.contract.MessageVersioning
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

@Schema(description = "A generic receipt confirming the delivery status of a notification on a specific channel.")
data class NotificationDeliveryReceipt(
    @field:Schema(description = "The correlation ID linking this receipt to the original request.", required = true)
    val correlationId: String,

    @field:Schema(description = "The specific channel this receipt pertains to (e.g., EMAIL, SMS).", required = true)
    val channel: NotificationChannel,

    @field:Schema(description = "The final delivery status of the notification.", required = true)
    val status: NotificationDeliveryStatus,

    @field:Schema(description = "The identifier of the recipient (e.g., email or phone number).", required = true)
    val recipientIdentifier: String,

    @field:Schema(description = "An error message, only present if the status is FAILED.")
    val errorMessage: String? = null,

    @field:Schema(description = "A map for any provider-specific details for auditing purposes.")
    val providerDetails: Map<String, Any> = emptyMap(),

    @field:Schema(description = "The unique ID of this receipt event.")
    val eventId: String = UUID.randomUUID().toString(),

    @field:Schema(description = "The version of this message contract.")
    val version: String = MessageVersioning.CURRENT_VERSION,

    @field:Schema(description = "The timestamp when this receipt was generated.")
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