import React, { createContext, useContext } from 'react';
import { useCart } from '../hooks/useCart';

// Create context with undefined default value
const CartContext = createContext<ReturnType<typeof useCart> | undefined>(undefined);

// Custom hook for using cart context
export const useCartContext = () => {
  const context = useContext(CartContext);
  if (context === undefined) {
    throw new Error('useCartContext must be used within a CartProvider');
  }
  return context;
};

// Provider component that wraps customer routes
export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const cart = useCart();
  return <CartContext.Provider value={cart}>{children}</CartContext.Provider>;
}; 