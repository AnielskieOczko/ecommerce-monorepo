package com.rj.ecommerce_backend.cart.exception

class CartNotFoundException(userId: String?) : RuntimeException("Cart not found for {}$userId")