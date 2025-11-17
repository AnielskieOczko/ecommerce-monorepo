package com.rj.ecommerce_backend.user.mapper

import com.rj.ecommerce_backend.api.shared.core.Email

import com.rj.ecommerce_backend.api.shared.dto.user.request.UserUpdateDetailsRequest
import com.rj.ecommerce_backend.api.shared.dto.user.common.UserBaseDetails
import com.rj.ecommerce_backend.api.shared.dto.user.request.AdminUpdateUserRequest
import com.rj.ecommerce_backend.api.shared.dto.user.response.UserResponse
import com.rj.ecommerce_backend.user.domain.User
import com.rj.ecommerce_backend.user.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class UserMapper(
    private val userRepository: UserRepository
) {

    fun updateUserFromBasicDetails(user: User, request: UserUpdateDetailsRequest) {
        user.firstName = request.firstName
        user.lastName = request.lastName
        user.address = request.address
        user.phoneNumber = request.phoneNumber
        user.dateOfBirth = request.dateOfBirth
    }

    fun updateUserFromAdminRequest(user: User, request: AdminUpdateUserRequest) {

        user.firstName = request.firstName

        user.lastName = request.lastName

        request.email?.let { newEmailString ->
            try {
                user.email = Email(newEmailString)
            } catch (e: IllegalArgumentException) {
                // Or ConstraintViolationException if using Bean Validation in Email's constructor
                // Decide how to handle: rethrow, wrap, or if mapper is part of a flow that handles it.
                // For a mapper, letting it propagate is often best.
                throw IllegalArgumentException("Admin update: Invalid email format or value provided: '$newEmailString'. ${e.message}", e)
            }
        }
        // If request.email is null, user.email (being non-nullable) remains UNCHANGED.

        user.address = request.address
        user.phoneNumber = request.phoneNumber
        user.dateOfBirth = request.dateOfBirth
        request.isActive.let {
            user.isActive = it
        }
        // If request.isActive is null, user.isActive (non-nullable) remains UNCHANGED.
        // Authority mapping MUST be handled in the UserService.

    }

    /**
     * Converts a User domain object into a UserInfoDTO.
     * This is the standard, safe way to create a DTO for API responses.
     *
     * @param user The User domain object to convert.
     * @return A fully populated UserInfoDTO.
     * @throws IllegalArgumentException if the User object is missing required fields (id, firstName, lastName)
     *                                  that are non-nullable in the DTO.
     */
    fun toUserResponse(user: User): UserResponse {
        // Use `requireNotNull` for clear, concise, and immediate validation.
        val userId = requireNotNull(user.id) { "User ID cannot be null for UserInfoDTO" }
        val userFirstName = requireNotNull(user.firstName) { "User firstName cannot be null for UserInfoDTO" }
        val userLastName = requireNotNull(user.lastName) { "User lastName cannot be null for UserInfoDTO" }

        // First, create the nested UserBaseDetails object.
        val userDetails = UserBaseDetails(
            firstName = userFirstName,
            lastName = userLastName,
            address = user.address,
            phoneNumber = user.phoneNumber,
            dateOfBirth = user.dateOfBirth
        )

        // Then, construct the final DTO using the nested object.
        return UserResponse(
            id = userId,
            userDetails = userDetails,
            email = user.email.value,
            authorities = user.authorities.map { it.name },
            isActive = user.isActive
        )
    }




}