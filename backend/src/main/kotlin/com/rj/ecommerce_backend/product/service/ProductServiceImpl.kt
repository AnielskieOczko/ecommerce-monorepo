package com.rj.ecommerce_backend.product.service

import com.rj.ecommerce.api.shared.core.ImageInfo
import com.rj.ecommerce.api.shared.core.ProductDescription
import com.rj.ecommerce.api.shared.core.ProductName
import com.rj.ecommerce.api.shared.core.QuantityInStock
import com.rj.ecommerce.api.shared.dto.product.ProductCreateRequestDTO
import com.rj.ecommerce.api.shared.dto.product.ProductResponseDTO
import com.rj.ecommerce.api.shared.dto.product.ProductUpdateRequestDTO
import com.rj.ecommerce_backend.product.domain.Category
import com.rj.ecommerce_backend.product.domain.Image
import com.rj.ecommerce_backend.product.domain.Product
import com.rj.ecommerce_backend.product.exception.CategoryNotFoundException
import com.rj.ecommerce_backend.product.exception.FileStorageException
import com.rj.ecommerce_backend.product.exception.ImageNotFoundException
import com.rj.ecommerce_backend.product.exception.InsufficientStockException
import com.rj.ecommerce_backend.product.exception.ProductNotFoundException
import com.rj.ecommerce_backend.product.mapper.ProductMapper
import com.rj.ecommerce_backend.product.repository.CategoryRepository
import com.rj.ecommerce_backend.product.repository.ImageRepository
import com.rj.ecommerce_backend.product.repository.ProductRepository
import com.rj.ecommerce_backend.product.search.ProductSearchCriteria
import io.github.oshai.kotlinlogging.KotlinLogging

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional
class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val imageRepository: ImageRepository,
    private val fileStorageService: FileStorageService,
    private val productMapper: ProductMapper
) : ProductService {

    companion object {
        private val logger = KotlinLogging.logger { }
    }

    override fun createProduct(
        productCreateRequestDTO: ProductCreateRequestDTO,
        images: List<MultipartFile>
    ): ProductResponseDTO {
        logger.info { "Creating new product: ${productCreateRequestDTO.productData.name}" }
        val product: Product = productMapper.toEntity(productCreateRequestDTO)

        // Handle images
        images.forEach { multipartFile ->
            try {
                // storeFile should ideally not require a pre-saved product if it can create an Image without product_id initially,
                // or if Product.addImage sets the link before cascade save.
                // For simplicity, let's assume Product.addImage adds to list, and JPA handles cascade.
                val imageName = multipartFile.originalFilename ?: "product_image_${System.currentTimeMillis()}"
                val imageEntity =
                    fileStorageService.storeFile(multipartFile, imageName, product) // Pass product to link
                product.addImage(imageEntity) // This should set image.product = product
            } catch (e: FileStorageException) {
                logger.error(e) { "Failed to store image ${multipartFile.originalFilename} for new product ${product.name.value}" }
                // Decide: continue without this image or fail the whole product creation?
                // For now, logging and continuing.
            }
        }

        val savedProduct = productRepository.save(product) // Save product with its images and categories
        logger.info { "Successfully created product ID: ${savedProduct.id}" }
        return productMapper.toDTO(savedProduct)
    }

    @Transactional(readOnly = true)
    override fun getProductById(productId: Long): ProductResponseDTO? {
        logger.debug { "Fetching product by ID: $productId" }

        return productRepository.findById(productId)
            .map { product -> productMapper.toDTO(product) }
            .orElse(null)
    }

    @Transactional(readOnly = true)
    override fun getProductEntityForValidation(productId: Long): Product? {
        logger.debug { "Fetching product entity for validation, ID: $productId" }

        return productRepository.findById(productId)
            .orElseThrow { ProductNotFoundException(productId) }
    }

    @Transactional(readOnly = true)
    override fun getAllProducts(
        pageable: Pageable,
        criteria: ProductSearchCriteria
    ): Page<ProductResponseDTO> {
        logger.debug { "Fetching all products with pageable: $pageable and criteria: $criteria" }
        val specification: Specification<Product> = criteria.toSpecification()

        val productsPage: Page<Product> = productRepository.findAll(specification, pageable)

        return productsPage.map { productMapper.toDTO(it) }
    }

    override fun updateProduct(
        productId: Long,
        request: ProductUpdateRequestDTO
    ): ProductResponseDTO {
        logger.info { "Updating product ID: $productId" }
        val product: Product = productRepository.findById(productId).orElseThrow {
            throw ProductNotFoundException(productId)
        }


        // product name can't be either blank or null
        request.name?.takeIf { it.isNotBlank() }?.let { validName -> ProductName(validName) }

        // product description can be blank but not null
        request.description?.let { newDescription -> ProductDescription(newDescription) }

        request.unitPrice.let { if (it != null) product.unitPrice = it }
        request.quantityInStock.let {
            if (it != null) {
                if (it >= 0) product.quantityInStock =
                    QuantityInStock(it) else throw IllegalArgumentException("Quantity in stock cannot be negative.")
            }
        }

        updateCategories(request, product)

        val savedProduct = productRepository.save(product)
        return productMapper.toDTO(savedProduct)
    }

    private fun updateCategories(
        request: ProductUpdateRequestDTO,
        product: Product
    ) {
        request.categoryIds?.let { newCategoryIdsFromRequest ->
            val desiredNewCategoryEntities: Set<Category> = if (newCategoryIdsFromRequest.isNotEmpty()) {
                val fetched = categoryRepository.findAllById(newCategoryIdsFromRequest).toSet()
                if (fetched.size != newCategoryIdsFromRequest.distinct().size) {
                    val foundIds = fetched.mapNotNull { it.id }
                    val missingIds = newCategoryIdsFromRequest.distinct().filterNot { foundIds.contains(it) }
                    throw CategoryNotFoundException("Some categories not found for update. Missing IDs: $missingIds")
                }
                fetched
            } else {
                emptySet()
            }

            val currentCategoriesInProduct: Set<Category> = product.categories.toSet()

            val categoriesToAdd = desiredNewCategoryEntities - currentCategoriesInProduct
            val categoriesToRemove = currentCategoriesInProduct - desiredNewCategoryEntities

            var changed = false
            if (categoriesToRemove.isNotEmpty()) {
                categoriesToRemove.forEach { product.removeCategory(it) }
                changed = true
            }
            if (categoriesToAdd.isNotEmpty()) {
                categoriesToAdd.forEach { product.addCategory(it) }
                changed = true
            }

            if (changed) {
                logger.info { "Categories updated for product ID ${product.id}. Added: ${categoriesToAdd.map { it.name }}, Removed: ${categoriesToRemove.map { it.name }}" }
            } else {
                logger.debug { "No effective change in categories for product ID ${product.id}" }
            }
        }
        // If request.categoryIds was null, no action is taken on categories (partial update).
    }

    override fun reduceProductQuantity(productId: Long, quantityInStockToReduce: Int) {
        logger.debug { "Reducing stock for product ID: $productId by $quantityInStockToReduce" }

        require(quantityInStockToReduce > 0) { "Quantity to reduce must be positive." }


        val product = productRepository.findById(productId)
            .orElseThrow { ProductNotFoundException(productId) }

        val currentStock = product.quantityInStock.value

        if (currentStock < quantityInStockToReduce) {
            throw InsufficientStockException("Insufficient stock for product $productId. Current: $currentStock, Requested reduction: $quantityInStockToReduce")
        }

        val newQuantity = currentStock - quantityInStockToReduce
        // This custom repository method is fine if it directly updates the DB field.
        // Otherwise, update the entity and save:
        // product.quantityInStock = QuantityInStock(newQuantity)
        // productRepository.save(product)
        productRepository.updateProductQuantity(
            productId,
            QuantityInStock(newQuantity)
        )
        logger.info { "Reduced stock for product ID: $productId. New stock: $newQuantity" }
    }

    override fun deleteProduct(productId: Long) {
        logger.info { "Deleting product ID: $productId" }
        if (!productRepository.existsById(productId)) {
            throw ProductNotFoundException(productId)
        }
        // Consider what happens to images on disk - fileStorageService.deleteProductFiles(productId)
        // Order items referencing this product? This can get complex. Soft delete is often safer.
        productRepository.deleteById(productId)
        logger.info { "Successfully deleted product ID: $productId" }
    }

    @Transactional(readOnly = true)
    override fun findProductsByCategory(categoryId: Long, pageable: Pageable): Page<ProductResponseDTO> {
        logger.debug { "Finding products by category ID: $categoryId with pageable: $pageable" }
        val productsPage: Page<Product> = productRepository.findByCategoriesId(categoryId, pageable)
        return productsPage.map { product -> productMapper.toDTO(product) }
    }

    @Transactional(readOnly = true)
    override fun findProductsByName(name: String, pageable: Pageable): Page<ProductResponseDTO> {
        logger.debug { "Finding products by name containing: '$name' with pageable: $pageable" }
        val productsPage: Page<Product> = productRepository
            .findByProductNameValueContainingIgnoreCase(name, pageable)
        return productsPage.map { product -> productMapper.toDTO(product) }
    }

    override fun addImagesToProduct(
        productId: Long,
        images: List<MultipartFile>,
        altText: String?
    ): ProductResponseDTO {
        val product: Product = productRepository.findById(productId).orElseThrow { ProductNotFoundException(productId) }

        if (images.isEmpty()) {
            logger.warn { "No image files provided for product ID: $productId in batch add." }
            return productMapper.toDTO(product)
        }

        images.forEachIndexed { index, img ->
            if (img.isEmpty) {
                logger.warn { "Skipping empty file at index $index for product ID: $productId." }
                return@forEachIndexed // Skips to the next file in the forEach
            }

            try {
                val altTextForThisImage = altText ?: img.originalFilename ?: "Product Image ${index + 1}"

                val image = fileStorageService.storeFile(img, altTextForThisImage, product)

                product.addImage(image)
            } catch (e: FileStorageException) {
                logger.error(e) { "Failed to store image ${img.originalFilename} for product ID $productId" }
                // log error and continue with the next image
            } catch (e: Exception) {
                logger.error(e) { "Unexpected error processing file ${img.originalFilename} for product ID $productId" }
            }
        }

        return productMapper.toDTO(productRepository.save(product))

    }


    override fun updateProductImageMetadata(
        productId: Long,
        imageId: Long,
        altText: String?
    ): ImageInfo {
        val image: Image = imageRepository.findById(imageId)
            .orElseThrow { ImageNotFoundException(imageId) }

        val product: Product = productRepository.findById(productId)
            .orElseThrow { ProductNotFoundException(productId) }

        if (image.product?.id != product.id) {
            logger.warn { "Attempt to update metadata for image ID $imageId which does not belong to product ID $productId. Image belongs to product ID ${image.product?.id}." }
            throw SecurityException("Image $imageId does not belong to product $productId.")
        }

        var metadataChanged = false
        altText?.let {
            if (image.altText != it) {
                image.altText = it
                metadataChanged = true
                logger.debug { "Updating altText for image ID $imageId to '$it'" }
            }
        }

        if (metadataChanged) {
            val savedImage = imageRepository.save(image)
            logger.info { "Successfully updated metadata for image ID: ${savedImage.id} on product ID: $productId" }
        } else {
            logger.info { "No metadata changes for image ID: $imageId on product ID: $productId. Returning current state." }
        }
        return mapImageEntityToInfoDTO(image)
    }

    private fun mapImageEntityToInfoDTO(image: Image): ImageInfo {
        return ImageInfo(
            id = image.id ?: throw IllegalStateException("Image ID cannot be null for DTO mapping"),
            path = image.path,
            altText = image.altText ?: "Product image",
            mimeType = image.mimeType
        )
    }

    override fun deleteProductImage(productId: Long, productImageId: Long) {
        logger.info { "Deleting image ID: $productImageId for product ID: $productId" }
        val product = productRepository.findById(productId)
            .orElseThrow { ProductNotFoundException(productId) }

        // Find the image within the product's collection
        val imageToDelete = product.images.find { it.id == productImageId }
            ?: throw ImageNotFoundException(productImageId)

        fileStorageService.deleteImage(imageToDelete)

        product.removeImage(imageToDelete)

        productRepository.save(product)
        logger.info { "Successfully deleted image ID: $productImageId for product ID: $productId" }
    }
}