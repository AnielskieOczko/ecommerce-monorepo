package com.rj.ecommerce.api.shared.dto.user

import jakarta.validation.constraints.NotBlank

data class AuthorityDTO(
    val id: Long?,
    @field:NotBlank(message = "Authority name cannot be empty.")
    val name: String
) {
}