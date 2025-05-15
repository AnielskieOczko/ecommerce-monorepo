package com.rj.ecommerce.api.shared.core

import java.time.LocalDateTime

@JvmRecord
data class ErrorDTO(
    val status: Int,
    val message: String?,
    val timestamp: LocalDateTime?
)
