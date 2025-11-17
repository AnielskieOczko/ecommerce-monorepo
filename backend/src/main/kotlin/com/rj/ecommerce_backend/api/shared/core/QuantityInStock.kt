package com.rj.ecommerce_backend.api.shared.core

import jakarta.persistence.Embeddable
import jakarta.validation.constraints.Min

@Embeddable
data class QuantityInStock(
    @field:Min(0)
    val value: Int
)