package com.rj.ecommerce_backend.notification.exception

class NotificationDispatchException(
    message: String,
    cause: Throwable? = null): RuntimeException(message, cause)
