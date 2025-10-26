package com.rj.notification_service.model

data class EmailModel(
    val to: String,
    val from: String,
    val subject: String,
    val htmlBody: String
)