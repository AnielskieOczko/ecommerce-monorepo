package com.rj.ecommerce.api.shared.messaging.notification.common

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A generic, top-level container for any notification request sent to the notification service.")
data class NotificationRequest<T>(
    @field:Schema(description = "The envelope containing routing and metadata for the notification.")
    val envelope: NotificationEnvelope,

    @field:Schema(description = "The specific business data payload for this notification.")
    val payload: T
)