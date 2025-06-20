package com.rj.ecommerce_backend.product.mapper

import com.rj.ecommerce.api.shared.core.ImageInfo
import com.rj.ecommerce.api.shared.core.ProductDescription
import com.rj.ecommerce.api.shared.core.ProductName
import com.rj.ecommerce.api.shared.core.QuantityInStock
import com.rj.ecommerce.api.shared.dto.product.ProductBase
import com.rj.ecommerce.api.shared.dto.product.category.CategoryDTO
import com.rj.ecommerce.api.shared.dto.product.ProductCreateRequestDTO
import com.rj.ecommerce.api.shared.dto.product.ProductResponseDTO
import com.rj.ecommerce_backend.product.domain.Category
import com.rj.ecommerce_backend.product.domain.Image
import com.rj.ecommerce_backend.product.domain.Product
import com.rj.ecommerce_backend.product.repository.CategoryRepository
import org.springframework.stereotype.Component

@Component
class ProductMapper(
    private val categoryRepository: CategoryRepository
) {

    fun toEntity(dto: ProductCreateRequestDTO): Product {

        val fetchedCategories: List<Category> = categoryRepository.findAllById(dto.productData.categoryIds)
        val images: MutableList<Image> = dto.images.map { info -> toImageEntity(info) }.toMutableList()

        val newProduct = Product(
            name = ProductName(dto.productData.name),
            description = ProductDescription(dto.productData.description),
            unitPrice = dto.productData.unitPrice,
            quantityInStock = QuantityInStock(dto.quantityInStock),
        )

        fetchedCategories.forEach { category -> newProduct.addCategory(category) }
        images.forEach { image -> newProduct.addImage(image) }

        return newProduct
    }

    fun toImageEntity(imageDTO: ImageInfo): Image {
        return Image(
            path = imageDTO.path,
            altText = imageDTO.altText,
            mimeType = imageDTO.mimeType,
        )
    }

    /**
     * Converts a Product domain object into a ProductResponseDTO, adhering to the refactored DTO structure.
     */
    fun toDTO(product: Product): ProductResponseDTO {
        val productId = requireNotNull(product.id) { "Product ID cannot be null for ProductResponseDTO" }

        // 1. ARCHITECTURAL FIX: Create the ProductBase object first.
        val productBase = ProductBase(
            name = requireNotNull(product.name?.value) { "Product name cannot be null" },
            description = requireNotNull(product.description?.value) { "Product description cannot be null" },
            unitPrice = product.unitPrice, // Assuming unitPrice is non-nullable in the domain
            categoryIds = product.categories.map { requireNotNull(it.id) { "Category in product list has a null ID" } }
        )

        // 2. Map the nested lists using clean helper functions.
        val categoriesDTO = product.categories.map { toCategoryDTO(it) }
        val imagesDTO = product.images.map { toImageInfoDTO(it) }

        // 3. Construct the final DTO using the new, correct structure.
        return ProductResponseDTO(
            id = productId,
            productData = productBase, // Use the composed base object
            quantity = requireNotNull(product.quantityInStock?.value) { "Product quantity cannot be null" },
            categories = categoriesDTO,
            images = imagesDTO
        )
    }

    // 4. HELPER FUNCTION: Encapsulates Category mapping logic.
    private fun toCategoryDTO(category: Category): CategoryDTO {
        return CategoryDTO(
            id = requireNotNull(category.id) { "Category ID cannot be null" },
            name = requireNotNull(category.name) { "Category name cannot be null" }
        )
    }

    // 5. HELPER FUNCTION: Encapsulates Image mapping logic and fixes the bug.
    private fun toImageInfoDTO(image: Image): ImageInfo {
        return ImageInfo(
            id = image.id, // ID can be nullable in the DTO
            path = requireNotNull(image.path) { "Image path cannot be null" },
            // BUG FIX: Throws an exception if required text/type is null instead of creating invalid data.
            altText = requireNotNull(image.altText) { "Image altText cannot be null" },
            mimeType = requireNotNull(image.mimeType) { "Image mimeType cannot be null" }
        )
    }
}

