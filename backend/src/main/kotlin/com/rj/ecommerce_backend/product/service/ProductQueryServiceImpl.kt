package com.rj.ecommerce_backend.product.service

import com.rj.ecommerce.api.shared.dto.product.response.ProductResponse
import com.rj.ecommerce_backend.product.domain.Product
import com.rj.ecommerce_backend.product.mapper.ProductMapper
import com.rj.ecommerce_backend.product.repository.ProductRepository
import com.rj.ecommerce_backend.product.search.ProductSearchCriteria
import com.rj.ecommerce_backend.product.service.image.ProductImageService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger { ProductQueryServiceImpl::class }

@Service
@Transactional(readOnly = true)
class ProductQueryServiceImpl(
    private val productRepository: ProductRepository,
    private val productImageService: ProductImageService,
    private val productMapper: ProductMapper
) : ProductQueryService {

    override fun getProductById(productId: Long): ProductResponse? {
        return productRepository.findById(productId)
            .map { product ->
                val imageInfos = productImageService.getImageInfosForProduct(product)
                productMapper.toDto(product, imageInfos)
            }.orElse(null)
    }

    override fun getAllProducts(pageable: Pageable, criteria: ProductSearchCriteria): Page<ProductResponse> {
        val spec = criteria.toSpecification()
        return productRepository.findAll(spec, pageable).map { product ->
            val imageInfos = productImageService.getImageInfosForProduct(product)
            productMapper.toDto(product, imageInfos)
        }
    }

    override fun findProductEntitiesByIds(productIds: List<Long>): List<Product> {
        return productRepository.findAllById(productIds)
    }

    override fun findProductsByCategory(categoryId: Long, pageable: Pageable): Page<ProductResponse> {
        val productsPage = productRepository.findByCategoriesId(categoryId, pageable).map { product ->
            val imageInfos = productImageService.getImageInfosForProduct(product)
            productMapper.toDto(product, imageInfos)
        }
        return productsPage
    }

    override fun findProductsByName(name: String, pageable: Pageable): Page<ProductResponse> {
        logger.debug { "Finding products by name containing: '$name' with pageable: $pageable" }
        val productsPage = productRepository
            .findByName_ValueContainingIgnoreCase(name, pageable).map { product ->
                val imageInfos = productImageService.getImageInfosForProduct(product)
                productMapper.toDto(product, imageInfos)
            }
        return productsPage
    }

    override fun getProductEntityForValidation(productId: Long): Product? {
        return productRepository.findById(productId).orElse(null)
    }
}