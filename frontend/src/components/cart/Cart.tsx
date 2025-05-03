import React, { useEffect, useState, useCallback } from 'react';
import { ErrorBoundary } from '../../components/ErrorBoundary';
import { CartItemDTO } from '../../types/cart';
import CartItem from './CartItem';
import { Button, Alert, Spinner } from '../ui';
import { XMarkIcon } from '../icons';

interface CartError {
  message: string;
  type: 'error' | 'warning';
}

const Cart: React.FC = () => {
  const [items, setItems] = useState<CartItemDTO[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<CartError | null>(null);
  const [isClearing, setIsClearing] = useState(false);

  const formatPrice = useCallback((price: number) => {
    return new Intl.NumberFormat('pl-PL', {
      style: 'currency',
      currency: 'PLN',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(price);
  }, []);

  const fetchCart = useCallback(async () => {
    try {
      setIsLoading(true);
      const response = await fetch('/api/cart');
      if (!response.ok) throw new Error('Nie udało się pobrać koszyka');
      const data = await response.json();
      setItems(data);
      setError(null);
    } catch (err) {
      setError({ message: 'Wystąpił błąd podczas ładowania koszyka', type: 'error' });
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchCart();
  }, [fetchCart]);

  const handleUpdateQuantity = async (itemId: number, quantity: number) => {
    try {
      const response = await fetch(`/api/cart/items/${itemId}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ quantity }),
      });

      if (!response.ok) throw new Error('Nie udało się zaktualizować ilości');

      setItems((prev) => prev.map((item) => (item.id === itemId ? { ...item, quantity } : item)));
      setError(null);
    } catch (err) {
      setError({ message: 'Nie udało się zaktualizować ilości produktu', type: 'error' });
    }
  };

  const handleRemoveItem = async (itemId: number) => {
    try {
      const response = await fetch(`/api/cart/items/${itemId}`, {
        method: 'DELETE',
      });

      if (!response.ok) throw new Error('Nie udało się usunąć produktu');

      setItems((prev) => prev.filter((item) => item.id !== itemId));
      setError(null);
    } catch (err) {
      setError({ message: 'Nie udało się usunąć produktu', type: 'error' });
    }
  };

  const handleClearCart = async () => {
    if (!window.confirm('Czy na pewno chcesz wyczyścić koszyk?')) return;

    try {
      setIsClearing(true);
      const response = await fetch('/api/cart', {
        method: 'DELETE',
      });

      if (!response.ok) throw new Error('Nie udało się wyczyścić koszyka');

      setItems([]);
      setError(null);
    } catch (err) {
      setError({ message: 'Nie udało się wyczyścić koszyka', type: 'error' });
    } finally {
      setIsClearing(false);
    }
  };

  const totalAmount = items.reduce((sum, item) => sum + item.price * item.quantity, 0);

  return (
    <ErrorBoundary>
      <div className="max-w-4xl mx-auto p-4">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold">Koszyk</h1>
          {items.length > 0 && (
            <Button
              variant="secondary"
              onClick={handleClearCart}
              disabled={isClearing}
              isLoading={isClearing}
            >
              <XMarkIcon className="w-5 h-5 mr-2" />
              Wyczyść koszyk
            </Button>
          )}
        </div>

        {error && (
          <Alert
            type={error.type}
            message={error.message}
            onClose={() => setError(null)}
            className="mb-4"
          />
        )}

        {isLoading ? (
          <div className="flex justify-center items-center h-64">
            <Spinner size="lg" />
          </div>
        ) : items.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-gray-500">Twój koszyk jest pusty</p>
          </div>
        ) : (
          <>
            <div className="bg-white rounded-lg shadow-sm divide-y divide-gray-200">
              {items.map((item) => (
                <CartItem
                  key={item.id}
                  item={item}
                  onUpdateQuantity={handleUpdateQuantity}
                  onRemove={handleRemoveItem}
                />
              ))}
            </div>

            <div className="mt-6 flex justify-end">
              <div className="text-right">
                <p className="text-sm text-gray-600">Suma</p>
                <p className="text-2xl font-bold">{formatPrice(totalAmount)}</p>
              </div>
            </div>
          </>
        )}
      </div>
    </ErrorBoundary>
  );
};

export default Cart;
