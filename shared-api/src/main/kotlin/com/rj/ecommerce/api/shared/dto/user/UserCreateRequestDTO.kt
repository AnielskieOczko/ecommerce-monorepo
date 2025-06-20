package com.rj.ecommerce.api.shared.dto.user

import com.fasterxml.jackson.annotation.JsonUnwrapped
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@Schema(description = "Request to create a new user account.")
data class UserCreateRequestDTO(
    @field:Valid
    @field:JsonUnwrapped
    val userDetails: UserBaseDetails,

    @field:Schema(description = "User's unique email address.", example = "john.doe@example.com")
    @field:Email @field:NotBlank
    val email: String,

    @field:Schema(description = "User's password. Must meet security requirements.", example = "Str0ngP@ssw0rd!")
    @field:NotBlank
    val password: String,

    @field:Schema(description = "Set of authorities/roles to assign to the user.", example = "[\"ROLE_USER\"]")
    val authorities: Set<String>
)
