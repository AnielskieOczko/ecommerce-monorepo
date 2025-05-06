package com.rj.ecommerce.api.shared.dto.user

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.PhoneNumber
import java.time.LocalDate

/**
 * Request to update user details (can be used by user or admin contextually).
 *
 * @property firstName Updated first name.
 * @property lastName Updated last name.
 * @property address Updated address.
 * @property phoneNumber Updated phone number.
 * @property dateOfBirth Updated date of birth.
 * @property authorities Updated list of authorities/roles (admin only).
 * @property isActive Updated active status (admin only).
 *
 * Requirements:
 * - All fields are optional, allowing partial updates
 * - authorities and isActive should only be modifiable by admins
 */
data class UserUpdateRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val address: Address? = null,
    val phoneNumber: PhoneNumber? = null,
    val dateOfBirth: LocalDate? = null,
    val authorities: List<String>? = null,
    val isActive: Boolean? = null
)
