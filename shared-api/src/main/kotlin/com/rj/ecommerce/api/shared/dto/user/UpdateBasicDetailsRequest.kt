package com.rj.ecommerce.api.shared.dto.user

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.PhoneNumber
import java.time.LocalDate

@JvmRecord
data class UpdateBasicDetailsRequest(
    val firstName: String?,
    val lastName: String?,
    val address: Address?,
    val phoneNumber: PhoneNumber?,
    val dateOfBirth: LocalDate?
)
