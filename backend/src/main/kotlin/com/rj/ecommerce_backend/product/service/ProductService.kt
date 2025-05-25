package com.rj.ecommerce_backend.product.service

import com.rj.ecommerce.api.shared.core.ImageInfo
import com.rj.ecommerce.api.shared.dto.product.ProductCreateRequestDTO
import com.rj.ecommerce.api.shared.dto.product.ProductResponseDTO
import com.rj.ecommerce.api.shared.dto.product.ProductUpdateRequestDTO
import com.rj.ecommerce_backend.product.domain.Product
import com.rj.ecommerce_backend.product.search.ProductSearchCriteria
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.multipart.MultipartFile


interface ProductService {

    fun createProduct(
        productCreateRequestDTO: ProductCreateRequestDTO,
        images: List<MultipartFile>
    ): ProductResponseDTO
    fun getProductById(productId: Long): ProductResponseDTO?
    fun getProductEntityForValidation(productId: Long): Product?
    fun getAllProducts(
        pageable: Pageable,
        criteria: ProductSearchCriteria
    ): Page<ProductResponseDTO>
    fun updateProduct(productId: Long, request: ProductUpdateRequestDTO): ProductResponseDTO
    fun reduceProductQuantity(productId: Long, quantityInStockToReduce: Int)
    fun deleteProduct(productId: Long)
    fun findProductsByCategory(categoryId: Long, pageable: Pageable): Page<ProductResponseDTO>
    fun findProductsByName(name: String, pageable: Pageable): Page<ProductResponseDTO>


    // product image actions
    fun addImagesToProduct(productId: Long, images: List<MultipartFile>, altText: String?): ProductResponseDTO
    fun updateProductImageMetadata(productId: Long, imageId: Long, altText: String?): ImageInfo
    fun deleteProductImage(productId: Long, productImageId: Long)




}