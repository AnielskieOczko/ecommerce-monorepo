package com.rj.ecommerce_backend.api.shared.dto.user.request

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.PhoneNumber
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import java.time.LocalDate

@Schema(description = "Request for an admin to update a user's details. All fields are optional.")
data class AdminUpdateUserRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    @field:Email
    val email: String? = null,
    val address: Address? = null,
    val phoneNumber: PhoneNumber? = null,
    val dateOfBirth: LocalDate? = null,
    val authorities: Set<String>? = null,
    val isActive: Boolean
)
