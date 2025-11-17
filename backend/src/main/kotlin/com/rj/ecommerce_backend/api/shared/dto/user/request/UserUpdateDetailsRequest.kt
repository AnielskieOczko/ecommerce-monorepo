package com.rj.ecommerce_backend.api.shared.dto.user.request

import com.rj.ecommerce_backend.api.shared.core.Address
import com.rj.ecommerce_backend.api.shared.core.PhoneNumber
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Request for a user to update their own basic details. All fields are optional; only provided fields will be updated.")
data class UserUpdateDetailsRequest(
    @field:Schema(description = "Updated first name.", example = "Jonathan")
    val firstName: String? = null,

    @field:Schema(description = "Updated last name.", example = "Doe")
    val lastName: String? = null,

    @field:Schema(description = "Updated primary address.")
    val address: Address? = null,

    @field:Schema(description = "Updated primary phone number.")
    val phoneNumber: PhoneNumber? = null,

    @field:Schema(description = "Updated date of birth.", example = "1990-01-16")
    val dateOfBirth: LocalDate? = null
)
