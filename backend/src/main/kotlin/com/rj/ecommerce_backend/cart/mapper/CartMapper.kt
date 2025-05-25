package com.rj.ecommerce_backend.cart.mapper

import com.rj.ecommerce.api.shared.dto.cart.CartDTO
import com.rj.ecommerce.api.shared.dto.cart.CartItemDTO
import com.rj.ecommerce.api.shared.dto.product.ProductSummaryDTO
import com.rj.ecommerce_backend.cart.domain.Cart
import com.rj.ecommerce_backend.cart.domain.CartItem
import com.rj.ecommerce_backend.product.domain.Product
import com.rj.ecommerce_backend.user.domain.User
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class CartMapper {

    companion object {
        private val logger = KotlinLogging.logger { }
    }

    fun toDto(cart: Cart): CartDTO {

        val cartItemsDto: List<CartItemDTO> = cart.cartItems.map { cartItem ->
            toDto(cartItem)
        }.toList()

        return CartDTO(
            id = cart.id,
            userId = cart.user?.id,
            items = cartItemsDto,
            createdAt = cart.createdAt,
            updatedAt = cart.updatedAt
        )
    }

    fun toDto(cartItem: CartItem): CartItemDTO {

        val productSummaryDTO = ProductSummaryDTO(
            id = cartItem.product.id
                ?: throw IllegalStateException("Product in CartItem ${cartItem.id} must have an ID."),
            name = cartItem.product.name?.value,
            unitPrice = cartItem.product.unitPrice
        )

        val cartItemDto = CartItemDTO(
            id = cartItem.id,
            product = productSummaryDTO,
            quantity = cartItem.quantity
        )

        return cartItemDto
    }

    fun toNewEntity(cartDto: CartDTO, owner: User): Cart {
        logger.debug { "Mapping CartDTO to new Cart entity for user ID: ${owner.id}" }
        return Cart(
            user = owner // The User entity is provided by the service
            // cartItems will be an empty mutableListOf() by default from Cart constructor
        )
    }

    fun toNewEntity(cartItemDto: CartItemDTO, productEntity: Product): CartItem {
        logger.debug { "Mapping CartItemDTO to new CartItem entity for product ID: ${productEntity.id}" }
        return CartItem(
            product = productEntity, // The actual Product entity is provided by the service
            quantity = cartItemDto.quantity
            // cart property will be set by Cart.addCartItem()
        )
    }


}