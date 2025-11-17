package com.rj.ecommerce_backend.cart.service

import com.rj.ecommerce_backend.api.shared.dto.cart.response.CartResponse


interface CartService {

    fun getCartForUser(userId: Long): CartResponse
    fun addItemToCart(userId: Long, productId: Long, quantity: Int): CartResponse
    fun updateCartItemQuantity(userId: Long, cartItemId: Long, newQuantity: Int): CartResponse
    fun removeItemFromCart(userId: Long, cartItemId: Long): CartResponse
    fun clearCart(userId: Long): CartResponse
}