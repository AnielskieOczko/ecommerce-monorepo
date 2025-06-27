package com.rj.ecommerce_backend.notification.service

import com.rj.ecommerce.api.shared.messaging.email.EmailDeliveryReceiptDTO
import com.rj.ecommerce_backend.notification.Notification

/**
 * A service for orchestrating the entire lifecycle of a notification.
 */
interface NotificationService {

    /**
     * Persists and dispatches a notification to the appropriate channel provider.
     * This is the primary entry point for sending a new notification.
     *
     * @param notification A transient Notification object, pre-populated with all necessary data and context.
     * @return The persisted Notification entity, now including its database ID and generated correlationId.
     */
    fun dispatch(notification: Notification): Notification

    /**
     * Updates the status of a notification based on a delivery receipt from a provider.
     *
     * @param receipt The delivery receipt DTO containing the correlation ID and new status.
     */
    fun updateStatusFromReceipt(receipt: EmailDeliveryReceiptDTO)

    /**
     * Finds a notification by its unique correlation ID.
     *
     * @param correlationId The ID to search for.
     * @return The found Notification, or null if it doesn't exist.
     */
    fun findByCorrelationId(correlationId: String): Notification?
}