package com.rj.ecommerce_backend.product.mapper

import com.rj.ecommerce.api.shared.core.ImageInfo
import com.rj.ecommerce.api.shared.core.ProductDescription
import com.rj.ecommerce.api.shared.core.ProductName
import com.rj.ecommerce.api.shared.core.QuantityInStock
import com.rj.ecommerce.api.shared.dto.product.ProductBase
import com.rj.ecommerce.api.shared.dto.product.ProductCreateRequestDTO
import com.rj.ecommerce.api.shared.dto.product.ProductResponseDTO
import com.rj.ecommerce.api.shared.dto.product.ProductUpdateRequestDTO
import com.rj.ecommerce.api.shared.dto.product.category.CategoryDTO
import com.rj.ecommerce_backend.product.domain.Category
import com.rj.ecommerce_backend.product.domain.Product
import org.springframework.stereotype.Component

@Component
class ProductMapper {

    /**
     * Creates a new Product entity from a DTO.
     * Requires the service to provide the pre-fetched Category entities.
     */
    fun createEntityFromDto(dto: ProductCreateRequestDTO, categories: List<Category>): Product {
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
    fun updateEntityFromDto(product: Product, dto: ProductUpdateRequestDTO, categories: List<Category>?) {
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
    fun toDto(product: Product, imageInfos: List<ImageInfo>): ProductResponseDTO {
        val productId = requireNotNull(product.id) { "Product ID cannot be null" }

        val productBase = ProductBase(
            name = product.name.value,
            description = product.description?.value ?: "",
            unitPrice = product.unitPrice,
            categoryIds = product.categories.mapNotNull { it.id }
        )

        val categoriesDTO = product.categories.map { toCategoryDTO(it) }

        return ProductResponseDTO(
            id = productId,
            productData = productBase,
            quantity = product.quantityInStock.value,
            categories = categoriesDTO,
            images = imageInfos // Use the pre-constructed list
        )
    }

    private fun toCategoryDTO(category: Category): CategoryDTO {
        return CategoryDTO(
            id = requireNotNull(category.id) { "Category ID cannot be null" },
            name = category.name
        )
    }
}