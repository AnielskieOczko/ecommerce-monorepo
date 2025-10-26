package com.rj.ecommerce_backend.cart.repository

import com.rj.ecommerce_backend.cart.domain.Cart
import com.rj.ecommerce_backend.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CartRepository: JpaRepository<Cart, Long> {

    @Query("SELECT c FROM Cart c JOIN FETCH c.cartItems WHERE c.user = :user")
    fun findByUser(user: User): Cart?
}