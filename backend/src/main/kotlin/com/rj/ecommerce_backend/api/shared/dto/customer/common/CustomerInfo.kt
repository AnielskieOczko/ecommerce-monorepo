package com.rj.ecommerce_backend.api.shared.dto.customer.common

import com.rj.ecommerce.api.shared.core.PhoneNumber
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A reusable component containing basic information about a customer.")
data class CustomerInfo(
    @field:Schema(description = "The unique ID of the customer.", example = "42")
    val id: String,

    @field:Schema(description = "The customer's first name.", example = "John")
    val firstName: String?,

    @field:Schema(description = "The customer's last name.", example = "Doe")
    val lastName: String?,

    @field:Schema(description = "The customer's email address.", example = "customer@example.com")
    val email: String,

    @field:Schema(description = "The customer's phone number.")
    val phoneNumber: PhoneNumber? = null
)