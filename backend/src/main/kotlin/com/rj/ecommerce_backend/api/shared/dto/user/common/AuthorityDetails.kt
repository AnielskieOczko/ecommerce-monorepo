package com.rj.ecommerce_backend.api.shared.dto.user.common

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Represents a user authority/role. Used for both requests and responses.")
data class AuthorityDetails(
    @field:Schema(description = "The unique identifier of the authority.", example = "2")
    val id: Long?,

    @field:Schema(description = "The name of the authority, e.g., 'ROLE_ADMIN'.", example = "ROLE_EDITOR", required = true)
    @field:NotBlank(message = "Authority name cannot be blank.")
    val name: String
)
