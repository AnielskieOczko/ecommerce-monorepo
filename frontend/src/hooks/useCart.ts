import { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import cartService from '../services/cartService';
import { CartDTO } from '../types/cart';

export const useCart = () => {
  const [cart, setCart] = useState<CartDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const { user } = useAuth();

  const fetchCart = async () => {
    try {
      // Don't fetch cart for admin users or when there's no user
      if (!user?.id || user?.roles?.includes('ROLE_ADMIN')) {
        setCart(null);
        setLoading(false);
        return;
      }

      const cartData = await cartService.getCart(user.id);
      setCart(cartData);
    } catch (error) {
      console.error('Error fetching cart:', error);
      setCart(null);
    } finally {
      setLoading(false);
    }
  };

  const addToCart = async (userId: number, productId: number, quantity: number) => {
    try {
      await cartService.addToCart(userId, productId, quantity);
      await fetchCart();
    } catch (error) {
      console.error('Error adding to cart:', error);
      throw error;
    }
  };

  useEffect(() => {
    fetchCart();
  }, [user?.id]);

  const getItemCount = () => {
    if (!cart?.cartItems) return 0;
    return cart.cartItems.reduce((total, item) => total + item.quantity, 0);
  };

  return {
    cart,
    loading,
    fetchCart,
    addToCart,
    getItemCount,
  };
};

export default useCart;
