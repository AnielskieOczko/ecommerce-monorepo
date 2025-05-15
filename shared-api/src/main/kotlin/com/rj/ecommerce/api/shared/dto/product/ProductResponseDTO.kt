package com.rj.ecommerce.api.shared.dto.product

import com.rj.ecommerce.api.shared.core.ImageInfo
import com.rj.ecommerce.api.shared.core.Money

data class ProductResponseDTO(
    val id: Long,
    val name: String,
    val description: String,
    val price: Money,
    val quantity: Int,
    val categories: List<CategoryDTO>,
    val images: List<ImageInfo>
)