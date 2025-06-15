package com.rj.ecommerce.api.shared.dto.user

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.PhoneNumber
import java.time.LocalDate

data class UpdateBasicDetailsRequestDTO(
    val firstName: String? = null,
    val lastName: String? = null,
    val address: Address? = null,
    val phoneNumber: PhoneNumber? = null,
    val dateOfBirth: LocalDate? = null
)
