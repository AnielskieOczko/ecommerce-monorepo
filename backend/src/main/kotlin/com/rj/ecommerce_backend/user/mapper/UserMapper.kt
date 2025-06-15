package com.rj.ecommerce_backend.user.mapper

import com.rj.ecommerce.api.shared.core.Email

import com.rj.ecommerce.api.shared.dto.user.AdminUpdateUserRequestDTO
import com.rj.ecommerce.api.shared.dto.user.UpdateBasicDetailsRequestDTO
import com.rj.ecommerce.api.shared.dto.user.UserInfoDTO
import com.rj.ecommerce_backend.user.domain.User
import com.rj.ecommerce_backend.user.repositories.UserRepository
import org.springframework.stereotype.Component

@Component
class UserMapper(
    private val userRepository: UserRepository
) {

    fun updateUserFromBasicDetails(user: User, request: UpdateBasicDetailsRequestDTO) {
        user.firstName = request.firstName
        user.lastName = request.lastName
        user.address = request.address
        user.phoneNumber = request.phoneNumber
        user.dateOfBirth = request.dateOfBirth
    }

    fun updateUserFromAdminRequest(user: User, request: AdminUpdateUserRequestDTO) {

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

    fun toUserInfoDTO(user: User): UserInfoDTO {
        val userEmailValue = user.email.value

        return UserInfoDTO(
            id = user.id ?: throw IllegalStateException("User ID cannot be null for UserInfoDTO"),
            firstName = user.firstName ?: "",
            lastName = user.lastName ?: "",
            email = userEmailValue,
            address = user.address,
            phoneNumber = user.phoneNumber,
            dateOfBirth = user.dateOfBirth,
            authorities = user.authorities.map { authority -> authority.name }.toList(),
            isActive = user.isActive // User.isActive is Boolean (non-nullable)
        )
    }




}