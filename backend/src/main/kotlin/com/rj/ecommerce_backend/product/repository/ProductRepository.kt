package com.rj.ecommerce_backend.product.repository

import com.rj.ecommerce.api.shared.core.QuantityInStock
import com.rj.ecommerce_backend.product.domain.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository :
    JpaRepository<Product, Long>,
    JpaSpecificationExecutor<Product> {

    @Query("SELECT p FROM Product p WHERE p.productName.value LIKE %:name%")
    fun findProductByNameLike(@Param("name") name: String): List<Product>

    fun findByCategoriesId(categoryId: Long, pageable: Pageable): Page<Product>

    fun findByProductNameValueContainingIgnoreCase(
        productName: String,
        pageable: Pageable
    ): Page<Product>

    @Modifying
    @Query("UPDATE Product p SET p.quantityInStock = :newQuantityInStock WHERE p.id = :productId")
    fun updateProductQuantity(
        @Param("productId") productId: Long,
        @Param("newQuantityInStock") newQuantityInStock: QuantityInStock
    )

    fun countByCategories_Id(categoryId: Long): Int

}