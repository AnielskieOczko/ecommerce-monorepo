package com.rj.ecommerce_backend.api.shared.dto.user.request

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.rj.ecommerce.api.shared.dto.user.common.UserBaseDetails
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Request to create a new user account.")
data class UserCreateRequest(
    @field:Valid
    @field:JsonUnwrapped
    val userDetails: UserBaseDetails,

    @field:Schema(description = "User's unique email address.", example = "john.doe@example.com", required = true)
    @field:Email @field:NotBlank
    val email: String,

    @field:Schema(description = "User's password. Must meet security requirements.", example = "Str0ngP@ssw0rd!", required = true)
    @field:NotBlank
    @field:Size(min = 8, message = "Password must be at least 8 characters long")
    val password: String,

    @field:Schema(description = "Set of authorities/roles to assign to the user.", example = "[\"ROLE_USER\"]")
    val authorities: Set<String> = setOf("ROLE_USER")
)
