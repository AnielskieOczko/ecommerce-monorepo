package com.rj.ecommerce.api.shared.core

import jakarta.persistence.Embeddable
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

@Embeddable
data class StockQuantity(
    @field:Min(0)
    val value: Int
)