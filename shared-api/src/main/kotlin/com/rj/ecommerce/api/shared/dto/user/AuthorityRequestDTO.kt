package com.rj.ecommerce.api.shared.dto.user

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Request model for creating or updating a user authority/role.")
data class AuthorityRequestDTO(
    @field:Schema(description = "The name of the authority, e.g., 'ROLE_ADMIN'.", example = "ROLE_EDITOR")
    @field:NotBlank(message = "Authority name cannot be blank.")
    val name: String
)