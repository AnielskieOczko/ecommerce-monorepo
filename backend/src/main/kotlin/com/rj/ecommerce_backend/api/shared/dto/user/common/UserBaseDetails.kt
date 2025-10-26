package com.rj.ecommerce_backend.api.shared.dto.user.common

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.PhoneNumber
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

@Schema(description = "Base model for a user's personal details.")
data class UserBaseDetails(
    @field:Schema(description = "User's first name.", example = "John")
    @field:NotBlank
    val firstName: String,

    @field:Schema(description = "User's last name.", example = "Doe")
    @field:NotBlank
    val lastName: String,

    @field:Schema(description = "User's primary address.")
    val address: Address? = null,

    @field:Schema(description = "User's primary phone number.")
    val phoneNumber: PhoneNumber? = null,

    @field:Schema(description = "User's date of birth.", example = "1990-01-15")
    val dateOfBirth: LocalDate? = null
)