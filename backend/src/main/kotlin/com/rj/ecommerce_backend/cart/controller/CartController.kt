package com.rj.ecommerce_backend.cart.controller

import com.rj.ecommerce.api.shared.dto.cart.AddItemToCartRequestDTO
import com.rj.ecommerce.api.shared.dto.cart.CartDTO
import com.rj.ecommerce.api.shared.dto.cart.UpdateCartItemQuantityRequest
import com.rj.ecommerce_backend.cart.service.CartService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users/{userId}/cart")
@PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
class CartController(
    private val cartService: CartService
) {
    companion object {
        private val logger = KotlinLogging.logger { }
    }


    @GetMapping
    fun getUserCart(@PathVariable userId: Long): ResponseEntity<CartDTO> {
        logger.info { "Request to get cart for user ID: $userId" }
        val cartDto = cartService.getCartForUser(userId)

        return ResponseEntity.ok(cartDto)
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("#userId == authentication.principal.id")
    fun addItemToCart(
        @PathVariable userId: Long,
        @Valid @RequestBody addItemRequest: AddItemToCartRequestDTO
    ): ResponseEntity<CartDTO> {
        logger.info { "Request to add item to cart for user ID: $userId. ProductID: ${addItemRequest.productId}, Quantity: ${addItemRequest.quantity}" }
        val updatedCartDto = cartService.addItemToCart(
            userId,
            addItemRequest.productId,
            addItemRequest.quantity
        )
        // Consider returning 201 Created if a new cart item resource was conceptually created,
        // or 200 OK if it's just updating the cart state. 200 OK is common.
        return ResponseEntity.ok(updatedCartDto)
    }

    @PutMapping("/items/{cartItemId}")
    @PreAuthorize("#userId == authentication.principal.id")
    fun updateCartItemQuantity(
        @PathVariable userId: Long,
        @PathVariable cartItemId: Long,
        @Valid @RequestBody updateRequest: UpdateCartItemQuantityRequest
    ): ResponseEntity<CartDTO> {
        logger.info { "Request to update quantity for cart item ID: $cartItemId to ${updateRequest.newQuantity} for user ID: $userId" }
        val updatedCartDto = cartService.updateCartItemQuantity(
            userId,
            cartItemId,
            updateRequest.newQuantity
        )
        return ResponseEntity.ok(updatedCartDto)
    }

    @DeleteMapping("/items/{cartItemId}")
    @PreAuthorize("#userId == authentication.principal.id")
    fun removeItemFromCart(
        @PathVariable userId: Long,
        @PathVariable cartItemId: Long
    ): ResponseEntity<CartDTO> {
        logger.info { "Request to remove item ID: $cartItemId from cart for user ID: $userId" }
        val updatedCartDto = cartService.removeItemFromCart(userId, cartItemId)
        return ResponseEntity.ok(updatedCartDto)
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#userId == authentication.principal.id")
    fun clearUserCart(@PathVariable userId: Long) {
        logger.info { "Request to clear cart for user ID: $userId" }
        cartService.clearCart(userId)
        logger.info { "Cart cleared successfully for user ID: $userId" }
    }

}