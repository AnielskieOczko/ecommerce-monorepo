package com.rj.ecommerce_backend.notification.service

import com.rj.ecommerce.api.shared.enums.NotificationChannel
import com.rj.ecommerce_backend.notification.Notification

interface NotificationProvider {
    fun send(notification: Notification)
    fun getChannel(): NotificationChannel
}