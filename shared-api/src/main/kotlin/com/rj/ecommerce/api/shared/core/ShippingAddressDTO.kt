package com.rj.ecommerce.api.shared.core

import jakarta.validation.constraints.NotBlank

@JvmRecord
data class ShippingAddressDTO(
    val street: @NotBlank String?,
    val city: @NotBlank String?,
    val zipCode: @NotBlank String?,
    val country: @NotBlank String?
)
