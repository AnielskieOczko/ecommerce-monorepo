import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import CartItem from '../../../components/cart/CartItem';
import cartService from '../../../services/cartService';
import { CartDTO } from '../../../types/cart';
import { useAuth } from '../../../contexts/AuthContext';
import { XMarkIcon } from '../../../components/icons';
import { ErrorBoundary } from '../../../components/ErrorBoundary';
import { Dialog } from '../../../components/ui';

interface CartError {
  message: string;
  code?: string;
}

interface ConfirmDialogProps {
  isOpen: boolean;
  title: string;
  message: string;
  onConfirm: () => void;
  onCancel: () => void;
}

const ConfirmDialog: React.FC<ConfirmDialogProps> = ({
  isOpen,
  title,
  message,
  onConfirm,
  onCancel,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4">
        <h2 className="text-xl font-semibold mb-4">{title}</h2>
        <p className="text-gray-600 mb-6">{message}</p>
        <div className="flex justify-end gap-4">
          <button
            onClick={onCancel}
            className="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-md transition-colors"
          >
            Anuluj
          </button>
          <button
            onClick={onConfirm}
            className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors"
          >
            Potwierdź
          </button>
        </div>
      </div>
    </div>
  );
};

const Cart: React.FC = () => {
  const [cart, setCart] = useState<CartDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<CartError | null>(null);
  const [isClearingCart, setIsClearingCart] = useState(false);
  const [showClearCartConfirm, setShowClearCartConfirm] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();
  const [notification, setNotification] = useState<{
    show: boolean;
    message: string;
    type: 'success' | 'error';
  }>({
    show: false,
    message: '',
    type: 'success',
  });

  const fetchCart = useCallback(async () => {
    try {
      if (!user?.id) {
        setError({ message: 'User ID is required' });
        return;
      }
      setLoading(true);
      const cartData = await cartService.getCart(user.id);
      setCart(cartData);
      setError(null);
    } catch (error) {
      console.error('Error fetching cart:', error);
      setError({ message: 'Nie udało się załadować koszyka' });
    } finally {
      setLoading(false);
    }
  }, [user?.id]);

  useEffect(() => {
    let mounted = true;

    const loadCart = async () => {
      if (mounted) {
        await fetchCart();
      }
    };

    loadCart();

    return () => {
      mounted = false;
    };
  }, [fetchCart]);

  const handleUpdateQuantity = useCallback(
    async (itemId: number, quantity: number) => {
      try {
        if (!user?.id) return;
        await cartService.updateCartItem(user.id, itemId, quantity);
        await fetchCart();
        showNotification('Ilość została zaktualizowana', 'success');
      } catch (error) {
        console.error('Error updating quantity:', error);
        showNotification('Nie udało się zaktualizować ilości', 'error');
      }
    },
    [user?.id, fetchCart]
  );

  const handleRemoveItem = useCallback(
    async (itemId: number) => {
      try {
        if (!user?.id) return;
        await cartService.removeFromCart(user.id, itemId);
        await fetchCart();
        showNotification('Produkt został usunięty z koszyka', 'success');
      } catch (error) {
        console.error('Error removing item:', error);
        showNotification('Nie udało się usunąć produktu', 'error');
      }
    },
    [user?.id, fetchCart]
  );

  const handleClearCart = async () => {
    try {
      if (!user?.id) return;
      setIsClearingCart(true);
      await cartService.clearCart(user.id);
      await fetchCart();
      showNotification('Koszyk został wyczyszczony', 'success');
      setShowClearCartConfirm(false);
    } catch (error) {
      console.error('Error clearing cart:', error);
      showNotification('Nie udało się wyczyścić koszyka', 'error');
    } finally {
      setIsClearingCart(false);
    }
  };

  const calculateTotal = useCallback(() => {
    if (!cart?.cartItems) return 0;
    return cart.cartItems.reduce((total, item) => total + item.price * item.quantity, 0);
  }, [cart?.cartItems]);

  const formatPrice = useMemo(() => {
    return (price: number) => {
      return new Intl.NumberFormat('pl-PL', {
        style: 'currency',
        currency: 'PLN',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      }).format(price);
    };
  }, []);

  const showNotification = useCallback((message: string, type: 'success' | 'error') => {
    setNotification({ show: true, message, type });
    const timer = setTimeout(() => {
      setNotification((prev) => ({ ...prev, show: false }));
    }, 3000);
    return () => clearTimeout(timer);
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[60vh]">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-black"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh]">
        <p className="text-red-600 text-lg mb-4">{error.message}</p>
        <button
          onClick={fetchCart}
          className="px-4 py-2 bg-black text-white rounded-md hover:bg-gray-800 transition-colors"
        >
          Spróbuj ponownie
        </button>
      </div>
    );
  }

  return (
    <ErrorBoundary>
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-3xl font-semibold mb-8">Koszyk</h1>

        {!cart?.cartItems || cart.cartItems.length === 0 ? (
          <div className="bg-white p-8 rounded-lg shadow text-center">
            <h2 className="text-xl font-semibold mb-4">Twój koszyk jest pusty</h2>
            <button
              onClick={() => navigate('/products')}
              className="bg-black text-white px-6 py-2 rounded-md hover:bg-gray-800 transition-colors"
            >
              Kontynuuj zakupy
            </button>
          </div>
        ) : (
          <div>
            <div className="bg-white rounded-lg shadow-md mb-8">
              {cart.cartItems.map((item) => (
                <CartItem
                  key={item.id}
                  item={item}
                  onUpdateQuantity={handleUpdateQuantity}
                  onRemove={handleRemoveItem}
                />
              ))}
            </div>

            <div className="flex justify-between items-center mt-8">
              <button
                onClick={() => setShowClearCartConfirm(true)}
                disabled={isClearingCart}
                className={`px-6 py-2 border border-red-600 text-red-600 rounded-md hover:bg-red-50 transition-colors ${
                  isClearingCart ? 'opacity-50 cursor-not-allowed' : ''
                }`}
              >
                {isClearingCart ? (
                  <span className="flex items-center">
                    <div className="w-4 h-4 border-2 border-red-600 border-t-transparent rounded-full animate-spin mr-2"></div>
                    Czyszczenie...
                  </span>
                ) : (
                  'Wyczyść koszyk'
                )}
              </button>

              <div className="text-right">
                <p className="text-2xl font-semibold mb-4">Suma: {formatPrice(calculateTotal())}</p>
                <button
                  onClick={() => navigate('/customer/checkout')}
                  className="bg-black text-white px-8 py-3 rounded-md hover:bg-gray-800 transition-colors"
                >
                  Przejdź do kasy
                </button>
              </div>
            </div>
          </div>
        )}

        <Dialog
          isOpen={showClearCartConfirm}
          title="Potwierdź wyczyszczenie koszyka"
          message="Czy na pewno chcesz usunąć wszystkie produkty z koszyka? Tej operacji nie można cofnąć."
          onConfirm={handleClearCart}
          onCancel={() => setShowClearCartConfirm(false)}
          variant="danger"
        />

        {notification.show && (
          <div
            className={`fixed top-4 right-4 p-4 rounded-lg shadow-lg flex items-center justify-between ${
              notification.type === 'success' ? 'bg-green-500' : 'bg-red-500'
            } text-white max-w-md animate-fade-in`}
          >
            <span>{notification.message}</span>
            <button
              onClick={() => setNotification((prev) => ({ ...prev, show: false }))}
              className="ml-4 text-white hover:text-gray-200"
            >
              <XMarkIcon className="w-5 h-5" />
            </button>
          </div>
        )}
      </div>
    </ErrorBoundary>
  );
};

export default Cart;
