package com.rj.ecommerce.api.shared.messaging.email

import com.rj.ecommerce.api.shared.enums.EmailTemplate
import java.time.LocalDateTime

interface EcommerceEmailRequest {
    val messageId: String
    val version: String
    val to: String
    val subject: String
    val template: EmailTemplate
    val timestamp: LocalDateTime
    fun getTemplateData(): Map<String, Any>
}