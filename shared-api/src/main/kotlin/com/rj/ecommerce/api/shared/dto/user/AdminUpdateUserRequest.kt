package com.rj.ecommerce.api.shared.dto.user

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.PhoneNumber
import jakarta.validation.constraints.Email
import java.time.LocalDate

@JvmRecord
data class AdminUpdateUserRequest(
    val firstName: String?,
    val lastName: String?,
    val email: @Email String?,
    val address: Address?,
    val phoneNumber: PhoneNumber?,
    val dateOfBirth: LocalDate?,
    val authorities: MutableSet<String?>?,
    val isActive: Boolean?
)
