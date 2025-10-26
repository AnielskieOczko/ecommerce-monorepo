package com.rj.ecommerce_backend.notification.provider.vendor.email

import com.rj.notification_service.model.EmailModel

/**
 * Defines the contract for a specific email delivery vendor (e.g., SMTP, SendGrid, AWS SES).
 */
interface EmailVendorProvider {
    fun getProviderType(): String
    fun send(email: EmailModel)
}