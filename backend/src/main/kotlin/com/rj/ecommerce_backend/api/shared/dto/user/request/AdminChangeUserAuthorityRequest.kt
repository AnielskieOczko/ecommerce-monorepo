package com.rj.ecommerce_backend.api.shared.dto.user.request

import jakarta.validation.constraints.NotEmpty


data class AdminChangeUserAuthorityRequest(
    @field:NotEmpty
    val authorities: Set<String>?)
