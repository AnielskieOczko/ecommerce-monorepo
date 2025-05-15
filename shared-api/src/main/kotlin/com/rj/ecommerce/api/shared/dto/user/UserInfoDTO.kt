package com.rj.ecommerce.api.shared.dto.user

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.PhoneNumber
import java.time.LocalDate

/**
 * Detailed user information for API responses.
 *
 * @property id User ID.
 * @property firstName First name of the user.
 * @property lastName Last name of the user.
 * @property email Email address of the user.
 * @property address Address of the user.
 * @property phoneNumber Phone number of the user.
 * @property dateOfBirth Date of birth of the user.
 * @property authorities List of authorities/roles assigned to the user.
 * @property isActive Whether the user account is active.
 *
 * Requirements:
 * - id, firstName, lastName, email, authorities, and isActive are required
 * - address, phoneNumber, and dateOfBirth are optional
 */
data class UserInfo(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val address: Address? = null,
    val phoneNumber: PhoneNumber? = null,
    val dateOfBirth: LocalDate? = null,
    val authorities: List<String>,
    val isActive: Boolean
)
