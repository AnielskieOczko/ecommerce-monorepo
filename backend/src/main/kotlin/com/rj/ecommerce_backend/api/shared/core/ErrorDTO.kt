package com.rj.ecommerce_backend.api.shared.core

import java.time.LocalDateTime

data class ErrorDTO(
    val status: Int,
    val message: String?,
    val timestamp: LocalDateTime?
)
