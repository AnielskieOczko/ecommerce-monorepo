package com.rj.ecommerce.api.shared.core

import java.time.LocalDateTime

data class ErrorDTO(
    val status: Int,
    val message: String?,
    val timestamp: LocalDateTime?
)
