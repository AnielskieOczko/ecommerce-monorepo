package com.rj.ecommerce.api.shared.dto.user

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.PhoneNumber
import jakarta.validation.constraints.Email
import java.time.LocalDate

data class AdminUpdateUserRequestDTO(
    val firstName: String?,
    val lastName: String?,
    @field:Email
    val email:String? = null,
    val address: Address?,
    val phoneNumber: PhoneNumber?,
    val dateOfBirth: LocalDate?,
    val authorities: Set<String>?,
    val isActive: Boolean
)
