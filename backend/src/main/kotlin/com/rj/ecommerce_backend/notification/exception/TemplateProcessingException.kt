package com.rj.ecommerce_backend.notification.exception

/**
 * Custom exception for template processing errors.
 */
class TemplateProcessingException(message: String, cause: Throwable) : RuntimeException(message, cause)