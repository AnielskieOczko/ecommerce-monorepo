package com.rj.ecommerce.api.shared.dto.product

import jakarta.validation.constraints.NotBlank


data class CategoryCreateRequestDTO(
    @field:NotBlank
    val name: String)
