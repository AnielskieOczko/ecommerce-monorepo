package com.rj.ecommerce.api.shared.dto.user

import jakarta.validation.constraints.NotBlank

data class AuthorityUpdateRequestDTO(
    @field:NotBlank(message = "New authority name cannot be blank.")
    val newName: String
) {
}