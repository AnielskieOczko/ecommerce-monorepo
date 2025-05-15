package com.rj.ecommerce_backend.product.mapper

import com.rj.ecommerce.api.shared.core.ImageInfo
import com.rj.ecommerce.api.shared.core.ProductDescription
import com.rj.ecommerce.api.shared.core.ProductName
import com.rj.ecommerce.api.shared.core.QuantityInStock
import com.rj.ecommerce.api.shared.dto.product.CategoryDTO
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

        val fetchedCategories: List<Category> = categoryRepository.findAllById(dto.categoryIds)
        val images: MutableList<Image> = dto.images.map { info -> toImageEntity(info) }.toMutableList()

        val newProduct = Product(
            name = ProductName(dto.name),
            description = ProductDescription(dto.description),
            unitPrice = dto.unitPrice,
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

    fun toDTO(product: Product): ProductResponseDTO {

        val productId = product.id
            ?: throw IllegalArgumentException("Product id cannot be null for ProductResponseDTO")

        val productName = product.name?.value
            ?: throw IllegalArgumentException("Product name cannot be null for ProductResponseDTO (ID: $productId)")

        val productDescription = product.description?.value
            ?: throw IllegalArgumentException("Product description cannot be null for ProductResponseDTO (ID: $productId)")

        val productQuantity = product.quantityInStock?.value
            ?: throw IllegalArgumentException("Product quantity cannot be null for ProductResponseDTO (ID: $productId)")

        val categoriesDTO = product.categories.map { category ->
            CategoryDTO(
                id = category.id ?: throw IllegalArgumentException("Category ID missing"),
                name = category.name ?: throw IllegalArgumentException("Category name is missing")
            )
        }

        val imagesDTO = product.images.map { image ->
            ImageInfo(
                id = image.id,
                path = image.path ?: throw IllegalArgumentException("Image path is missing"),
                altText = image.altText ?: "",
                mimeType = image.mimeType ?: "",
            )
        }

        return ProductResponseDTO(
            id = productId,
            name = productName,
            description = productDescription,
            price = product.unitPrice,
            quantity = productQuantity,
            categories = categoriesDTO,
            images = imagesDTO,
        )
    }

}