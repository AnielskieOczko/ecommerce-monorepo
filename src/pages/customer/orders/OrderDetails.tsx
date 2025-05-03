import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../contexts/AuthContext';
import { Button } from '../../../components/ui';
import { OrderStatus, PaymentMethod, ShippingMethod } from './types';

interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  price: number;
}

interface ShippingAddress {
  street: string;
  city: string;
  zipCode: string;
  country: string;
}

interface Order {
  id: number;
  userId: number;
  orderItems: OrderItem[];
  totalPrice: number;
  shippingAddress: ShippingAddress;
  shippingMethod: ShippingMethod;
  paymentMethod: PaymentMethod;
  paymentTransactionId: string | null;
  orderDate: string;
  orderStatus: OrderStatus;
  createdAt: string;
  updatedAt: string;
}

const OrderDetails: React.FC = () => {
  const { orderId } = useParams<{ orderId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchOrder = async () => {
      if (!user?.id || !orderId) return;

      try {
        setLoading(true);
        setError(null);

        const response = await fetch(
          `http://localhost:8080/api/v1/users/${user.id}/orders/${orderId}`,
          {
            headers: {
              Authorization: `Bearer ${localStorage.getItem('token')}`,
            },
          }
        );

        if (!response.ok) {
          if (response.status === 404) {
            throw new Error('Zamówienie nie zostało znalezione');
          }
          throw new Error('Nie udało się pobrać szczegółów zamówienia');
        }

        const data = await response.json();
        setOrder(data);
      } catch (err) {
        setError(
          err instanceof Error
            ? err.message
            : 'Wystąpił błąd podczas ładowania szczegółów zamówienia'
        );
      } finally {
        setLoading(false);
      }
    };

    fetchOrder();
  }, [orderId, user?.id]);

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('pl-PL', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('pl-PL', {
      style: 'currency',
      currency: 'PLN',
    }).format(price);
  };

  const getStatusBadgeColor = (status: OrderStatus) => {
    switch (status) {
      case OrderStatus.PENDING:
        return 'bg-yellow-100 text-yellow-800';
      case OrderStatus.CONFIRMED:
        return 'bg-blue-100 text-blue-800';
      case OrderStatus.PROCESSING:
        return 'bg-indigo-100 text-indigo-800';
      case OrderStatus.SHIPPED:
        return 'bg-purple-100 text-purple-800';
      case OrderStatus.DELIVERED:
        return 'bg-green-100 text-green-800';
      case OrderStatus.CANCELLED:
        return 'bg-red-100 text-red-800';
      case OrderStatus.REFUNDED:
        return 'bg-orange-100 text-orange-800';
      case OrderStatus.FAILED:
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const translateStatus = (status: OrderStatus) => {
    const statusMap: { [key in OrderStatus]: string } = {
      [OrderStatus.PENDING]: 'Oczekujące',
      [OrderStatus.CONFIRMED]: 'Potwierdzone',
      [OrderStatus.PROCESSING]: 'W realizacji',
      [OrderStatus.SHIPPED]: 'Wysłane',
      [OrderStatus.DELIVERED]: 'Dostarczone',
      [OrderStatus.CANCELLED]: 'Anulowane',
      [OrderStatus.REFUNDED]: 'Zwrócone',
      [OrderStatus.FAILED]: 'Nieudane',
    };
    return statusMap[status];
  };

  const translatePaymentMethod = (method: PaymentMethod) => {
    const methodMap: { [key in PaymentMethod]: string } = {
      [PaymentMethod.CREDIT_CARD]: 'Karta płatnicza',
      [PaymentMethod.PAYPAL]: 'PayPal',
      [PaymentMethod.BANK_TRANSFER]: 'Przelew bankowy',
      [PaymentMethod.BLIK]: 'BLIK',
    };
    return methodMap[method];
  };

  const translateShippingMethod = (method: ShippingMethod) => {
    const methodMap: { [key in ShippingMethod]: string } = {
      [ShippingMethod.INPOST]: 'InPost',
      [ShippingMethod.DHL]: 'DHL',
    };
    return methodMap[method];
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-black"></div>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="text-center py-8">
        <div className="text-red-600 mb-4">{error}</div>
        <Button onClick={() => navigate('/customer/orders')}>Wróć do listy zamówień</Button>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-4xl mx-auto">
        <div className="flex items-center justify-between mb-6">
          <button
            onClick={() => navigate('/customer/orders')}
            className="flex items-center text-gray-600 hover:text-black"
          >
            <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M15 19l-7-7 7-7"
              />
            </svg>
            Wróć do listy zamówień
          </button>
          <span
            className={`px-4 py-2 rounded-full text-sm ${getStatusBadgeColor(order.orderStatus)}`}
          >
            {translateStatus(order.orderStatus)}
          </span>
        </div>

        <div className="bg-white shadow rounded-lg overflow-hidden">
          <div className="p-6 border-b">
            <div className="flex justify-between items-start">
              <div>
                <h1 className="text-2xl font-semibold mb-2">Zamówienie #{order.id}</h1>
                <p className="text-gray-600">Data złożenia: {formatDate(order.orderDate)}</p>
              </div>
              <div className="text-right">
                <div className="text-lg font-semibold">{formatPrice(order.totalPrice)}</div>
                <div className="text-sm text-gray-500">
                  {order.paymentTransactionId ? 'Opłacone' : 'Oczekuje na płatność'}
                </div>
              </div>
            </div>
          </div>

          <div className="p-6 border-b">
            <h2 className="text-lg font-semibold mb-4">Produkty</h2>
            <div className="space-y-4">
              {order.orderItems.map((item) => (
                <div key={item.id} className="flex justify-between items-center">
                  <div>
                    <div className="font-medium">{item.productName}</div>
                    <div className="text-sm text-gray-500">Ilość: {item.quantity}</div>
                  </div>
                  <div className="text-right">
                    <div>{formatPrice(item.price * item.quantity)}</div>
                    <div className="text-sm text-gray-500">{formatPrice(item.price)} / szt.</div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="p-6 grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h2 className="text-lg font-semibold mb-4">Dane dostawy</h2>
              <div className="space-y-2">
                <p>
                  <span className="text-gray-600">Adres:</span>
                  <br />
                  {order.shippingAddress.street}
                  <br />
                  {order.shippingAddress.zipCode} {order.shippingAddress.city}
                  <br />
                  {order.shippingAddress.country}
                </p>
                <p>
                  <span className="text-gray-600">Metoda dostawy:</span>
                  <br />
                  {translateShippingMethod(order.shippingMethod)}
                </p>
              </div>
            </div>

            <div>
              <h2 className="text-lg font-semibold mb-4">Płatność</h2>
              <div className="space-y-2">
                <p>
                  <span className="text-gray-600">Metoda płatności:</span>
                  <br />
                  {translatePaymentMethod(order.paymentMethod)}
                </p>
                {order.paymentTransactionId && (
                  <p>
                    <span className="text-gray-600">ID transakcji:</span>
                    <br />
                    {order.paymentTransactionId}
                  </p>
                )}
              </div>
            </div>
          </div>

          <div className="p-6 bg-gray-50">
            <div className="max-w-sm ml-auto space-y-2">
              <div className="flex justify-between">
                <span>Wartość produktów:</span>
                <span>
                  {formatPrice(
                    order.orderItems.reduce((sum, item) => sum + item.price * item.quantity, 0)
                  )}
                </span>
              </div>
              <div className="flex justify-between">
                <span>Koszt dostawy:</span>
                <span>
                  {formatPrice(
                    order.totalPrice -
                      order.orderItems.reduce((sum, item) => sum + item.price * item.quantity, 0)
                  )}
                </span>
              </div>
              <div className="flex justify-between font-semibold text-lg pt-2 border-t">
                <span>Suma:</span>
                <span>{formatPrice(order.totalPrice)}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default OrderDetails;
