package com.rj.ecommerce_backend.notification.command

import com.rj.ecommerce_backend.api.shared.enums.NotificationChannel
import com.rj.ecommerce_backend.api.shared.enums.NotificationEntityType
import com.rj.ecommerce_backend.api.shared.enums.NotificationTemplate
import com.rj.ecommerce_backend.notification.context.NotificationContext

/**
 * A command DTO used internally by the backend to request the creation of a new notification record.
 */
data class CreateNotificationCommand(
    val recipient: String,
    val subject: String,
    val template: NotificationTemplate,
    val entityType: NotificationEntityType,
    val entityId: String,
    val context: NotificationContext,
    val channels: Set<NotificationChannel> // CHANGED: Now a Set
) {
    init {
        require(channels.isNotEmpty()) { "Notification channels set cannot be empty." }
    }
}