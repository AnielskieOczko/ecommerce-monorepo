package com.rj.ecommerce_backend.cart.mapper;

import com.rj.ecommerce.api.shared.core.Money;
import com.rj.ecommerce_backend.cart.domain.Cart;
import com.rj.ecommerce_backend.cart.domain.CartItem;
import com.rj.ecommerce_backend.product.domain.Product;
import com.rj.ecommerce.api.shared.dto.cart.CartDTO;
import com.rj.ecommerce.api.shared.dto.cart.CartItemDTO;
import com.rj.ecommerce.api.shared.dto.product.ProductSummaryDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CartMapper {

    public static CartDTO toDto(Cart cart) {
        if (cart == null) {
            return null;
        }

        List<CartItemDTO> cartItems = cart.getCartItems().stream()
                .map(CartMapper::toDto)
                .toList();

        return new CartDTO(
                cart.getId(),
                cart.getUser().getId(),
                cartItems,
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }

    public static CartItemDTO toDto(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        Product product = cartItem.getProduct();
        String productName = (product != null && product.getName() != null) ?
                product.getName().value : null;
        BigDecimal price = (product != null && product.getUnitPrice() != null) ?
                product.getUnitPrice().amount.value : null;

        // Create a ProductSummary for the shared CartItem
        assert productName != null;
        assert price != null;
        ProductSummaryDTO productSummary = new ProductSummaryDTO(
                product.getId(),
                null,
                productName,
                new Money(price, "PLN")
        );

        return new CartItemDTO(
                cartItem.getId(),
                productSummary,
                cartItem.getQuantity()
        );
    }

    public static Cart toEntity(CartDTO cartDto) {
        if (cartDto == null) {
            return null;
        }

        return Cart.builder()
                .id(cartDto.getId())
                // .user(userService.findById(cartDto.getUserId())) // Fetch user from database in your service
                .createdAt(cartDto.getCreatedAt())
                .updatedAt(cartDto.getUpdatedAt())
                .build();
    }

    public static CartItem toEntity(CartItemDTO cartItemDto) {
        if (cartItemDto == null) {
            return null;
        }

        return CartItem.builder()
                .id(cartItemDto.getId())
                // .cart(cartRepository.findById(cartItemDto.getCartId())) // Fetch cart from the database in your service
                // .product(productRepository.findById(cartItemDto.getProductId())) // Fetch product from the database
                .quantity(cartItemDto.getQuantity())
                .build();
    }
}