package com.rj.notification_service.exception

/**
 * Custom exception for template processing errors.
 */
class TemplateProcessingException(message: String, cause: Throwable) : RuntimeException(message, cause)