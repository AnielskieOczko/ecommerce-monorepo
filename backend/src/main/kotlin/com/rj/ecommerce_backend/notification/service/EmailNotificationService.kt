package com.rj.ecommerce_backend.notification

import com.rj.ecommerce.api.shared.enums.NotificationDispatchStatus
import com.rj.ecommerce.api.shared.enums.NotificationEntityType
import com.rj.ecommerce.api.shared.messaging.email.NotificationCreationRequest

interface EmailNotificationService {
    /**
     * Creates a record of a notification request. The notification starts in PENDING status.
     * @param request A DTO containing all necessary data to create the notification record.
     * @return The newly created and persisted EmailNotification entity.
     */
    fun createNotification(request: NotificationCreationRequest): EmailNotification

    /**
     * Marks a notification as successfully SENT.
     * @param messageId The unique ID of the message to update.
     * @throws NotificationNotFoundException if no notification with the given ID exists.
     */
    fun markAsSent(messageId: String)

    /**
     * Marks a notification as FAILED and records the reason.
     * @param messageId The unique ID of the message to update.
     * @param reason A descriptive error message explaining the failure.
     * @throws NotificationNotFoundException if no notification with the given ID exists.
     */
    fun markAsFailed(messageId: String, reason: String)

    // The query methods remain the same as they are clear and direct.
    fun getEmailNotificationsForEntity(entityType: NotificationEntityType, entityId: String): List<EmailNotification>
    fun getEmailNotificationsForRecipient(recipient: String): List<EmailNotification>
    fun getEmailNotificationsByStatus(status: NotificationDispatchStatus): List<EmailNotification>
}