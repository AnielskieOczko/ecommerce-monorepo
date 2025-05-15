package com.rj.ecommerce.api.shared.dto.user

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.PhoneNumber
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

/**
 * Request to create a new user.
 *
 * @property firstName First name of the user.
 * @property lastName Last name of the user.
 * @property email Email address of the user.
 * @property password Password for the user account.
 * @property address Address of the user.
 * @property phoneNumber Phone number of the user.
 * @property dateOfBirth Date of birth of the user.
 * @property authorities List of authorities/roles to assign to the user.
 *
 * Requirements:
 * - firstName, lastName, email, and password are required
 * - address, phoneNumber, dateOfBirth, and authorities are optional
 * - email must be a valid email format
 * - password must meet security requirements
 */
data class UserCreateRequestDTO(
    @field:NotBlank
    val firstName: String,
    @field:NotBlank
    val lastName: String,
    @field:Email @field:NotBlank
    val email: String,
    @field:NotBlank
    val password: String,
    val address: Address? = null,
    val phoneNumber: PhoneNumber? = null,
    val dateOfBirth: LocalDate? = null,
    val authorities: Set<String>
)
