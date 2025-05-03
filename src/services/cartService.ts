import { CartDTO, CartItemDTO, CartItemRequest } from '../types/cart';
import api from './api';

const CART_API_URL = '/api/v1/users';

export const getCart = async (userId: number): Promise<CartDTO> => {
  const response = await api.get(`${CART_API_URL}/${userId}/cart?userId=${userId}`);
  return response.data;
};

export const addToCart = async (
  userId: number,
  productId: number,
  quantity: number = 1
): Promise<CartDTO> => {
  const cartItemRequest: CartItemRequest = {
    productId,
    quantity,
  };
  const response = await api.post(
    `${CART_API_URL}/${userId}/cart/items?userId=${userId}`,
    cartItemRequest
  );
  return response.data;
};

export const updateCartItem = async (
  userId: number,
  cartItemId: number,
  quantity: number
): Promise<CartDTO> => {
  const response = await api.put(
    `${CART_API_URL}/${userId}/cart/items/${cartItemId}?userId=${userId}&quantity=${quantity}`
  );
  return response.data;
};

export const removeFromCart = async (userId: number, cartItemId: number): Promise<void> => {
  await api.delete(`${CART_API_URL}/${userId}/cart/items/${cartItemId}?userId=${userId}`);
};

export const clearCart = async (userId: number): Promise<void> => {
  await api.delete(`${CART_API_URL}/${userId}/cart?userId=${userId}`);
};

const cartService = {
  getCart,
  addToCart,
  updateCartItem,
  removeFromCart,
  clearCart,
};

export default cartService;
