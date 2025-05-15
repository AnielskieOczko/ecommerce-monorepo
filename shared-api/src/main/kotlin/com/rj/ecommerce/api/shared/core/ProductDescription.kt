package com.rj.ecommerce_backend.product.valueobject

import jakarta.persistence.Embeddable
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Embeddable
data class ProductDescription(
    @field:NotBlank
    @field:Size(min = 3, max = 255)
    val value: String
)
