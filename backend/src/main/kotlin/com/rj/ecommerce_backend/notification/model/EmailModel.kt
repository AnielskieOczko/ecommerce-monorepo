package com.rj.ecommerce_backend.notification.model

data class EmailModel(
    val to: String,
    val from: String,
    val subject: String,
    val htmlBody: String
)