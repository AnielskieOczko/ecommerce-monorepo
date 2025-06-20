package com.rj.ecommerce.api.shared.dto.user

import com.fasterxml.jackson.annotation.JsonUnwrapped
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@Schema(description = "Detailed user information for API responses.")
data class UserInfoDTO(
    @field:Schema(description = "User's unique ID.", example = "42")
    val id: Long,

    @field:Valid
    @field:JsonUnwrapped
    val userDetails: UserBaseDetails,

    @field:Schema(description = "User's unique email address.", example = "john.doe@example.com")
    @field:NotBlank @field:Email
    val email: String,

    @field:Schema(description = "List of authorities/roles assigned to the user.", example = "[\"ROLE_USER\"]")
    val authorities: List<String>,

    @field:Schema(description = "Indicates if the user account is active.", example = "true")
    val isActive: Boolean
)
