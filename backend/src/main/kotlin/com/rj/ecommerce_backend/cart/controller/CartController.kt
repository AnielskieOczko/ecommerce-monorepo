package com.rj.ecommerce_backend.cart.controller

import com.rj.ecommerce.api.shared.dto.cart.AddItemToCartRequestDTO
import com.rj.ecommerce.api.shared.dto.cart.CartDTO
import com.rj.ecommerce.api.shared.dto.cart.UpdateCartItemQuantityRequestDTO
import com.rj.ecommerce_backend.cart.service.CartService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "Cart", description = "APIs for managing user shopping carts")
@PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
class CartController(
    private val cartService: CartService
) {
    companion object {
        private val logger = KotlinLogging.logger { }
    }


    @GetMapping
    @Operation(summary = "Get user's cart", description = "Retrieves the full shopping cart for a given user.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved cart")
    @ApiResponse(responseCode = "404", description = "User or cart not found")
    @ApiResponse(responseCode = "403", description = "Forbidden access")
    fun getUserCart(@PathVariable userId: Long): ResponseEntity<CartDTO> {
        logger.info { "Request to get cart for user ID: $userId" }
        val cartDto = cartService.getCartForUser(userId)

        return ResponseEntity.ok(cartDto)
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add item to cart", description = "Adds a product with a specified quantity to the user's cart.")
    @ApiResponse(responseCode = "201", description = "Item successfully added and cart returned")
    @ApiResponse(responseCode = "400", description = "Invalid request data, e.g., negative quantity")
    @ApiResponse(responseCode = "404", description = "Product not found")
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
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedCartDto)
    }

    @PutMapping("/items/{cartItemId}")
    @Operation(summary = "Update cart item quantity", description = "Changes the quantity of a specific item in the cart.")
    @ApiResponse(responseCode = "200", description = "Quantity updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data, e.g., negative quantity")
    @ApiResponse(responseCode = "404", description = "Cart item not found")
    @PreAuthorize("#userId == authentication.principal.id")
    fun updateCartItemQuantity(
        @PathVariable userId: Long,
        @PathVariable cartItemId: Long,
        @Valid @RequestBody updateRequest: UpdateCartItemQuantityRequestDTO
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
    @Operation(summary = "Remove item from cart", description = "Removes a specific item from the cart.")
    @ApiResponse(responseCode = "200", description = "Item removed successfully")
    @ApiResponse(responseCode = "404", description = "Cart item not found")
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
    @Operation(summary = "Clear cart", description = "Removes all items from the user's cart.")
    @ApiResponse(responseCode = "204", description = "Cart cleared successfully")
    @PreAuthorize("#userId == authentication.principal.id")
    fun clearUserCart(@PathVariable userId: Long) {
        logger.info { "Request to clear cart for user ID: $userId" }
        cartService.clearCart(userId)
        logger.info { "Cart cleared successfully for user ID: $userId" }
    }

}