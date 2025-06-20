package com.rj.ecommerce.api.shared.dto.user

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.PhoneNumber
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Request for a user to update their own basic details. All fields are optional.")
data class UpdateBasicDetailsRequestDTO(
    @field:Schema(description = "Updated first name.", example = "Jonathan")
    val firstName: String? = null,

    @field:Schema(description = "Updated last name.", example = "Doe")
    val lastName: String? = null,

    @field:Schema(description = "Updated address.")
    val address: Address? = null,

    @field:Schema(description = "Updated phone number.")
    val phoneNumber: PhoneNumber? = null,

    @field:Schema(description = "Updated date of birth.", example = "1990-01-16")
    val dateOfBirth: LocalDate? = null
)
