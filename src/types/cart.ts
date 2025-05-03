export interface CartItemDTO {
  id: Long;
  cartId: Long;
  productId: Long;
  productName: string;
  quantity: number;
  price: BigDecimal;
}

export interface CartDTO {
  id: Long;
  userId: Long;
  cartItems: CartItemDTO[];
  createdAt: Date;
  updatedAt: Date;
}

// TypeScript equivalents for Java types
type Long = number;
type BigDecimal = number;

export interface CartItemRequest {
  productId: Long;
  quantity: number;
}
