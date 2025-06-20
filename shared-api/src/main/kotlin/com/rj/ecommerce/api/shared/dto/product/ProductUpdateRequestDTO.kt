package com.rj.ecommerce.api.shared.dto.product

import com.rj.ecommerce.api.shared.core.ImageInfo
import com.rj.ecommerce.api.shared.core.Money
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

@Schema(description = "Request to update an existing product. All fields are optional.")
data class ProductUpdateRequestDTO(
    @field:Schema(description = "Updated product name.", example = "Premium Wireless Keyboard v2")
    @field:Size(min = 3, max = 255)
    val name: String? = null,

    @field:Schema(description = "Updated product description.", example = "Now with improved battery life.")
    val description: String? = null,

    @field:Schema(description = "Updated product price.")
    val unitPrice: Money? = null,

    @field:Schema(description = "Updated product stock quantity.", example = "200")
    @field:Min(0)
    val quantityInStock: Int? = null,

    @field:Schema(description = "Updated list of category IDs.")
    val categoryIds: List<Long>? = null
)
