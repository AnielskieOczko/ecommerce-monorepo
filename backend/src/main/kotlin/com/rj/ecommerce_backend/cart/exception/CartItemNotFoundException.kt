package com.rj.ecommerce_backend.cart.exception

class CartItemNotFoundException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable?) : super(message, cause)

    constructor(cartItemId: Long) : super("Cart item not found with ID: $cartItemId")
    constructor(cartItemId: Long, cartId: Long?) : super("Cart item with ID $cartItemId not found in cart ${cartId ?: "unknown"}.")
}