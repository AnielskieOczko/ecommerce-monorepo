package com.rj.ecommerce.api.shared.dto.user

import jakarta.validation.constraints.NotEmpty


data class AdminChangeUserAuthorityRequestDTO(
    @field:NotEmpty
    val authorities: Set<String>?)
