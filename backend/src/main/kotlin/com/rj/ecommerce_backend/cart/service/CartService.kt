package com.rj.ecommerce_backend.cart.service

import com.rj.ecommerce.api.shared.dto.cart.CartDTO


interface CartService {

    fun getCartForUser(userId: Long): CartDTO
    fun addItemToCart(userId: Long, productId: Long, quantity: Int): CartDTO
    fun updateCartItemQuantity(userId: Long, cartItemId: Long, newQuantity: Int): CartDTO
    fun removeItemFromCart(userId: Long, cartItemId: Long): CartDTO
    fun clearCart(userId: Long): CartDTO
}