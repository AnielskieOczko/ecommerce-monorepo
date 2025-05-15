package com.rj.ecommerce.api.shared.dto.user

import jakarta.validation.constraints.NotBlank

data class AuthorityCreateRequestDTO(
    @field:NotBlank(message = "Authority name cannot be blank.")
    val name: String
) {
}