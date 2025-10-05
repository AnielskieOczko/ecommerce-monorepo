package com.rj.ecommerce_backend.product.repository

import com.rj.ecommerce_backend.product.domain.Image
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ImageRepository : JpaRepository<Image, Long> {

    fun findByFileIdentifier(identifier: String): Image?
}