package com.rj.ecommerce.api.shared.dto.product.common

import com.rj.ecommerce.api.shared.core.Money
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size


@Schema(description = "Base representation of core product data.")
data class ProductBase(
    @field:Schema(description = "The name of the product.", example = "Premium Wireless Keyboard", required = true)
    @field:NotBlank
    @field:Size(min = 3, max = 255)
    val name: String,

    @field:Schema(description = "A detailed description of the product.", example = "A mechanical, backlit, wireless keyboard.", required = true)
    @field:NotBlank
    val description: String,

    @field:Schema(description = "The price per unit of the product.", required = true)
    val unitPrice: Money,

    @field:Schema(description = "List of category IDs this product belongs to.")
    val categoryIds: List<Long> = emptyList()
)