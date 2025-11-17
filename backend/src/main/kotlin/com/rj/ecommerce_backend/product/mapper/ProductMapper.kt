package com.rj.ecommerce_backend.product.mapper

import com.rj.ecommerce_backend.api.shared.core.ImageInfo
import com.rj.ecommerce_backend.api.shared.core.ProductDescription
import com.rj.ecommerce_backend.api.shared.core.ProductName
import com.rj.ecommerce_backend.api.shared.core.QuantityInStock
import com.rj.ecommerce_backend.api.shared.dto.product.common.CategoryDetails
import com.rj.ecommerce_backend.api.shared.dto.product.common.ProductBase
import com.rj.ecommerce_backend.api.shared.dto.product.request.ProductCreateRequest
import com.rj.ecommerce_backend.api.shared.dto.product.request.ProductUpdateRequest
import com.rj.ecommerce_backend.api.shared.dto.product.response.ProductResponse
import com.rj.ecommerce_backend.product.domain.Category
import com.rj.ecommerce_backend.product.domain.Product
import org.springframework.stereotype.Component

@Component
class ProductMapper {

    /**
     * Creates a new Product entity from a DTO.
     * Requires the service to provide the pre-fetched Category entities.
     */
    fun createEntityFromDto(dto: ProductCreateRequest, categories: List<Category>): Product {
        val newProduct = Product(
            name = ProductName(dto.productData.name),
            description = ProductDescription(dto.productData.description),
            unitPrice = dto.productData.unitPrice,
            quantityInStock = QuantityInStock(dto.quantityInStock)
        )
        categories.forEach { newProduct.addCategory(it) }
        return newProduct
    }

    /**
     * Updates an existing Product entity from a DTO.
     * Requires the service to provide the pre-fetched Category entities if they are being updated.
     */
    fun updateEntityFromDto(product: Product, dto: ProductUpdateRequest, categories: List<Category>?) {
        dto.name?.let { product.name = ProductName(it) }
        dto.description?.let { product.description = ProductDescription(it) }
        dto.unitPrice?.let { product.unitPrice = it }
        dto.quantityInStock?.let { product.quantityInStock = QuantityInStock(it) }

        // If a list of categories is provided, update the association.
        categories?.let {
            product.categories.clear()
            it.forEach { category -> product.addCategory(category) }
        }
    }

    /**
     * Converts a Product domain object into a ProductResponseDTO.
     * Requires the service to provide the fully constructed ImageInfo DTOs,
     * as URL generation is an infrastructure concern.
     */
    fun toDto(product: Product, imageInfos: List<ImageInfo>): ProductResponse {
        val productId = requireNotNull(product.id) { "Product ID cannot be null" }

        val productBase = ProductBase(
            name = product.name.value,
            description = product.description?.value ?: "",
            unitPrice = product.unitPrice,
            categoryIds = product.categories.mapNotNull { it.id }
        )

        val categoriesDTO = product.categories.map { toCategoryDTO(it) }

        return ProductResponse(
            id = productId,
            productData = productBase,
            quantity = product.quantityInStock.value,
            categories = categoriesDTO,
            images = imageInfos // Use the pre-constructed list
        )
    }

    private fun toCategoryDTO(category: Category): CategoryDetails {
        return CategoryDetails(
            id = requireNotNull(category.id) { "Category ID cannot be null" },
            name = category.name
        )
    }
}