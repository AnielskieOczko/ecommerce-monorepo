import React, { useCallback, useState } from 'react';
import { CartItemDTO } from '../../types/cart';
import { TrashIcon } from '../icons';
import { Input, Button, Spinner } from '../ui';
import { debounce } from 'lodash';

interface CartItemProps {
  item: CartItemDTO;
  onUpdateQuantity: (itemId: number, quantity: number) => Promise<void>;
  onRemove: (itemId: number) => Promise<void>;
}

const CartItem: React.FC<CartItemProps> = React.memo(({ item, onUpdateQuantity, onRemove }) => {
  const [isUpdating, setIsUpdating] = useState(false);
  const [isRemoving, setIsRemoving] = useState(false);

  const debouncedUpdateQuantity = useCallback(
    debounce(async (id: number, quantity: number) => {
      try {
        setIsUpdating(true);
        await onUpdateQuantity(id, quantity);
      } finally {
        setIsUpdating(false);
      }
    }, 500),
    [onUpdateQuantity]
  );

  const handleQuantityChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    try {
      const newQuantity = Math.max(1, parseInt(event.target.value) || 0);
      if (newQuantity > 0 && newQuantity <= 99) {
        debouncedUpdateQuantity(item.id, newQuantity);
      }
    } catch (error) {
      console.error('Invalid quantity input:', error);
    }
  };

  const handleRemove = async () => {
    try {
      setIsRemoving(true);
      await onRemove(item.id);
    } finally {
      setIsRemoving(false);
    }
  };

  const formatPrice = useCallback((price: number) => {
    return new Intl.NumberFormat('pl-PL', {
      style: 'currency',
      currency: 'PLN',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(price);
  }, []);

  return (
    <div className="flex items-center p-4 border-b border-gray-200">
      <div className="flex-grow">
        <h3 className="text-lg font-semibold">{item.productName}</h3>
        <p className="text-sm text-gray-600">Cena: {formatPrice(item.price)}</p>
      </div>

      <div className="flex items-center gap-4">
        <div className="relative">
          <Input
            type="number"
            value={item.quantity}
            onChange={handleQuantityChange}
            min="1"
            max="99"
            disabled={isUpdating}
            aria-label="Ilość"
            className="w-20"
            error={item.quantity >= 99 ? 'Maksymalna ilość: 99' : undefined}
          />
          {isUpdating && (
            <div className="absolute inset-0 flex items-center justify-center bg-white bg-opacity-80">
              <Spinner size="sm" />
            </div>
          )}
        </div>
        <span className="text-base font-medium whitespace-nowrap">
          {formatPrice(item.price * item.quantity)}
        </span>
        <Button
          variant="danger"
          size="sm"
          onClick={handleRemove}
          disabled={isRemoving}
          isLoading={isRemoving}
          aria-label="Usuń produkt"
          className="!p-2"
        >
          <TrashIcon className="w-5 h-5" />
        </Button>
      </div>
    </div>
  );
});

CartItem.displayName = 'CartItem';

export default CartItem;
