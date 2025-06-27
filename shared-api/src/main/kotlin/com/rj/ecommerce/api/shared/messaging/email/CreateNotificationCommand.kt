package com.rj.ecommerce.api.shared.messaging.email

import com.rj.ecommerce.api.shared.enums.NotificationTemplate
import com.rj.ecommerce.api.shared.enums.NotificationEntityType

/**
 * A command DTO used internally by the backend to request the creation of a new notification record.
 * This is NOT a message sent over RabbitMQ.
 */
data class CreateNotificationCommand(
    val recipient: String,
    val subject: String,
    val template: NotificationTemplate,
    val entityType: NotificationEntityType,
    val entityId: String
)