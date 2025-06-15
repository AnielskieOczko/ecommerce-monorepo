package com.rj.ecommerce.api.shared.messaging.email

import com.rj.ecommerce.api.shared.enums.EmailTemplate
import com.rj.ecommerce.api.shared.enums.NotificationEntityType

data class NotificationCreationRequest(
    val messageId: String,
    val recipient: String,
    val subject: String,
    val template: EmailTemplate,
    val entityType: NotificationEntityType,
    val entityId: String
)