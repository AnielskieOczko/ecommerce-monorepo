package com.rj.ecommerce_backend.cart.service

import com.rj.ecommerce_backend.api.shared.dto.cart.response.CartResponse
import com.rj.ecommerce_backend.cart.domain.Cart
import com.rj.ecommerce_backend.cart.domain.CartItem
import com.rj.ecommerce_backend.cart.exception.CartItemNotFoundException
import com.rj.ecommerce_backend.cart.exception.CartNotFoundException
import com.rj.ecommerce_backend.cart.mapper.CartMapper
import com.rj.ecommerce_backend.cart.repository.CartRepository
import com.rj.ecommerce_backend.product.exception.ProductNotFoundException
import com.rj.ecommerce_backend.product.repository.ProductRepository
import com.rj.ecommerce_backend.security.SecurityContext
import com.rj.ecommerce_backend.user.domain.User
import com.rj.ecommerce_backend.user.exception.UserNotFoundException
import com.rj.ecommerce_backend.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CartServiceImpl(
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val securityContext: SecurityContext,
    private val cartMapper: CartMapper
) : CartService {

    companion object {
        private val logger = KotlinLogging.logger { }
        private const val USER_NOT_FOUND_MSG_PREFIX = "User not found with id: "
        private const val CART_NOT_FOUND_FOR_USER_MSG_PREFIX = "Cart not found for user id: "
        private const val CART_ITEM_NOT_FOUND_MSG_PREFIX = "Cart item not found with id: "
    }

    @Transactional(readOnly = true)
    override fun getCartForUser(userId: Long): CartResponse {
        logger.debug { "Fetching cart for user ID: $userId" }
        securityContext.ensureAccess(userId) // Authorize

        val user: User = userRepository.findById(userId).orElseThrow {
            throw UserNotFoundException(USER_NOT_FOUND_MSG_PREFIX + userId)
        }

        val cart = getOrCreateCartForUser(user)

        return cartMapper.toDto(cart)
    }

    override fun addItemToCart(userId: Long, productId: Long, quantity: Int): CartResponse {
        logger.info { "Adding product ID: $productId with quantity: $quantity to cart for user ID: $userId" }
        securityContext.ensureAccess(userId)

        if (quantity <= 0) {
            throw IllegalArgumentException("Quantity to add must be positive.")
        }

        val user: User = userRepository.findById(userId).orElseThrow {
            throw UserNotFoundException(USER_NOT_FOUND_MSG_PREFIX + userId)
        }

        val cart = getOrCreateCartForUser(user)

        val product = productRepository.findById(productId)
            .orElseThrow {
                logger.warn { "Product not found with ID: $productId when adding to cart for user ID: $userId" }
                ProductNotFoundException(productId)
            }

        val itemToAdd = CartItem(product = product, quantity = quantity, cart = cart)
        cart.addCartItem(itemToAdd)

        val savedCart = cartRepository.save(cart)
        logger.info { "Product ID: $productId (qty: $quantity) added/updated in cart ID: ${savedCart.id} for user ID: $userId" }
        return cartMapper.toDto(savedCart)

    }

    override fun updateCartItemQuantity(userId: Long, cartItemId: Long, newQuantity: Int): CartResponse {
        logger.info { "Updating quantity for cart item ID: $cartItemId to $newQuantity for user ID: $userId" }
        securityContext.ensureAccess(userId)

        val user = findUserOrThrow(userId)
        val cart = cartRepository.findByUser(user)
            ?: run {
                logger.warn { "Cart not found for user ID $userId while trying to update cart item $cartItemId." }
                throw CartNotFoundException("$CART_NOT_FOUND_FOR_USER_MSG_PREFIX$userId")
            }

        val cartItemToUpdate = cart.cartItems.find { it.id == cartItemId }
            ?: run {
                logger.warn { "Cart item ID $cartItemId not found in cart ID ${cart.id} for user ID $userId." }
                throw CartItemNotFoundException("$CART_ITEM_NOT_FOUND_MSG_PREFIX$cartItemId in cart ${cart.id}")
            }


        if (newQuantity <= 0) {
            logger.info { "New quantity for cart item ID $cartItemId is $newQuantity. Removing item from cart." }
            cart.removeCartItem(cartItemToUpdate)
        } else {
            cartItemToUpdate.quantity = newQuantity
            logger.info { "Updated quantity for cart item ID $cartItemId to $newQuantity." }
        }

        val savedCart = cartRepository.save(cart)
        return cartMapper.toDto(savedCart)

    }

    override fun removeItemFromCart(userId: Long, cartItemId: Long): CartResponse {
        logger.info { "Removing cart item ID: $cartItemId for user ID: $userId" }
        securityContext.ensureAccess(userId)

        val user = findUserOrThrow(userId)
        val cart = cartRepository.findByUser(user)
            ?: run {
                logger.warn { "Cart not found for user ID $userId when trying to remove item $cartItemId." }
                throw CartNotFoundException("$CART_NOT_FOUND_FOR_USER_MSG_PREFIX$userId")
            }

        val cartItemToRemove = cart.cartItems.find { it.id == cartItemId }
            ?: run {
                logger.warn { "Cart item ID $cartItemId not found in cart ID ${cart.id} for user ID $userId for removal." }
                throw CartItemNotFoundException("$CART_ITEM_NOT_FOUND_MSG_PREFIX$cartItemId in cart ${cart.id}")
            }

        cart.removeCartItem(cartItemToRemove)
        val savedCart = cartRepository.save(cart)
        logger.info { "Removed cart item ID $cartItemId from cart ID ${savedCart.id} for user ID $userId." }
        return cartMapper.toDto(savedCart)

    }

    override fun clearCart(userId: Long): CartResponse {
        logger.info { "Clearing cart for user ID: $userId" }
        securityContext.ensureAccess(userId)

        val user = findUserOrThrow(userId)
        val cart = cartRepository.findByUser(user)
            ?: run {
                // If no cart, effectively it's already clear. Return an empty cart DTO.
                logger.info { "No cart found for user ID $userId to clear. Returning representation of an empty cart." }
                val emptyCartRepresentation = Cart(user = user) // Transient, new empty cart for DTO mapping
                return cartMapper.toDto(emptyCartRepresentation)
            }

        cart.clearCart()
        val savedCart = cartRepository.save(cart)
        logger.info { "Cart ID ${savedCart.id} cleared for user ID $userId." }
        return cartMapper.toDto(savedCart)
    }

    private fun findUserOrThrow(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow {
                logger.warn { "$USER_NOT_FOUND_MSG_PREFIX$userId (Caller: ${securityContext.getCurrentUser().id})" }
                UserNotFoundException("$USER_NOT_FOUND_MSG_PREFIX$userId")
            }
    }

    private fun getOrCreateCartForUser(user: User): Cart {
        return cartRepository.findByUser(user)
            ?: run {
                logger.info { "No existing cart found for user ${user.id}, creating a new one." }
                val newCart = Cart(user = user)
                user.cart = newCart
                return cartRepository.save(newCart)
            }
    }


}