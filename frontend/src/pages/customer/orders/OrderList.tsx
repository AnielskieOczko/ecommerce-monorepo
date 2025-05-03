import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../contexts/AuthContext';
import { Button } from '../../../components/ui';

enum OrderStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  PROCESSING = 'PROCESSING',
  SHIPPED = 'SHIPPED',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED',
  REFUNDED = 'REFUNDED',
  FAILED = 'FAILED',
}

enum PaymentMethod {
  CREDIT_CARD = 'CREDIT_CARD',
  PAYPAL = 'PAYPAL',
  BANK_TRANSFER = 'BANK_TRANSFER',
  BLIK = 'BLIK',
}

enum ShippingMethod {
  INPOST = 'INPOST',
  DHL = 'DHL',
}

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

interface OrdersResponse {
  content: Order[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

interface OrderFilters {
  search?: string;
  status?: OrderStatus;
  minTotal?: number;
  maxTotal?: number;
  startDate?: string;
  endDate?: string;
  paymentMethod?: PaymentMethod;
  hasTransactionId?: boolean;
}

const OrderList: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [sortField, setSortField] = useState('orderDate');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('desc');
  const [filters, setFilters] = useState<OrderFilters>({});
  const [isFiltersVisible, setIsFiltersVisible] = useState(false);

  const buildQueryString = () => {
    const params = new URLSearchParams();
    params.append('page', currentPage.toString());
    params.append('size', '10');
    params.append('sort', `${sortField}:${sortDirection}`);

    if (filters.search) params.append('search', filters.search);
    if (filters.status) params.append('status', filters.status);
    if (filters.minTotal) params.append('minTotal', filters.minTotal.toString());
    if (filters.maxTotal) params.append('maxTotal', filters.maxTotal.toString());
    if (filters.startDate) params.append('startDate', filters.startDate);
    if (filters.endDate) params.append('endDate', filters.endDate);
    if (filters.paymentMethod) params.append('paymentMethod', filters.paymentMethod);
    if (filters.hasTransactionId !== undefined)
      params.append('hasTransactionId', filters.hasTransactionId.toString());

    return params.toString();
  };

  const fetchOrders = async () => {
    if (!user?.id) return;

    try {
      setLoading(true);
      setError(null);

      const queryString = buildQueryString();
      const response = await fetch(
        `http://localhost:8080/api/v1/users/${user.id}/orders?${queryString}`,
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`,
          },
        }
      );

      if (!response.ok) {
        throw new Error('Nie udało się pobrać zamówień');
      }

      const data: OrdersResponse = await response.json();
      setOrders(data.content);
      setTotalPages(data.totalPages);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Wystąpił błąd podczas ładowania zamówień');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, [user?.id, currentPage, sortField, sortDirection, filters]);

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

  const handleFilterChange = (name: keyof OrderFilters, value: any) => {
    setFilters((prev) => ({ ...prev, [name]: value }));
    setCurrentPage(0); // Reset to first page when filters change
  };

  const renderFilters = () => (
    <div className="mb-6 p-4 bg-gray-50 rounded-lg">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-lg font-semibold">Filtry</h2>
        <button onClick={() => setFilters({})} className="text-sm text-gray-600 hover:text-black">
          Wyczyść filtry
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div>
          <label className="block text-sm font-medium mb-1">Wyszukaj</label>
          <input
            type="text"
            value={filters.search || ''}
            onChange={(e) => handleFilterChange('search', e.target.value)}
            className="w-full border rounded-md px-3 py-2"
            placeholder="Szukaj w zamówieniach..."
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Status</label>
          <select
            value={filters.status || ''}
            onChange={(e) => handleFilterChange('status', e.target.value || undefined)}
            className="w-full border rounded-md px-3 py-2"
          >
            <option value="">Wszystkie</option>
            {Object.values(OrderStatus).map((status) => (
              <option key={status} value={status}>
                {translateStatus(status as OrderStatus)}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Metoda płatności</label>
          <select
            value={filters.paymentMethod || ''}
            onChange={(e) => handleFilterChange('paymentMethod', e.target.value || undefined)}
            className="w-full border rounded-md px-3 py-2"
          >
            <option value="">Wszystkie</option>
            {Object.values(PaymentMethod).map((method) => (
              <option key={method} value={method}>
                {translatePaymentMethod(method as PaymentMethod)}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Minimalna wartość</label>
          <input
            type="number"
            value={filters.minTotal || ''}
            onChange={(e) =>
              handleFilterChange('minTotal', e.target.value ? Number(e.target.value) : undefined)
            }
            className="w-full border rounded-md px-3 py-2"
            min="0"
            step="0.01"
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Maksymalna wartość</label>
          <input
            type="number"
            value={filters.maxTotal || ''}
            onChange={(e) =>
              handleFilterChange('maxTotal', e.target.value ? Number(e.target.value) : undefined)
            }
            className="w-full border rounded-md px-3 py-2"
            min="0"
            step="0.01"
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Data od</label>
          <input
            type="datetime-local"
            value={filters.startDate || ''}
            onChange={(e) => handleFilterChange('startDate', e.target.value || undefined)}
            className="w-full border rounded-md px-3 py-2"
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Data do</label>
          <input
            type="datetime-local"
            value={filters.endDate || ''}
            onChange={(e) => handleFilterChange('endDate', e.target.value || undefined)}
            className="w-full border rounded-md px-3 py-2"
          />
        </div>

        <div className="flex items-center">
          <label className="flex items-center">
            <input
              type="checkbox"
              checked={filters.hasTransactionId || false}
              onChange={(e) => handleFilterChange('hasTransactionId', e.target.checked)}
              className="rounded border-gray-300 text-black focus:ring-black mr-2"
            />
            <span className="text-sm">Tylko opłacone</span>
          </label>
        </div>
      </div>
    </div>
  );

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-black"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-8">
        <div className="text-red-600 mb-4">{error}</div>
        <Button onClick={fetchOrders}>Spróbuj ponownie</Button>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-semibold">Moje zamówienia</h1>
        <div className="flex items-center space-x-4">
          <button
            onClick={() => setIsFiltersVisible(!isFiltersVisible)}
            className="flex items-center space-x-1 text-sm hover:text-gray-600"
          >
            <span>{isFiltersVisible ? 'Ukryj filtry' : 'Pokaż filtry'}</span>
            <svg
              className={`w-4 h-4 transform transition-transform ${
                isFiltersVisible ? 'rotate-180' : ''
              }`}
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M19 9l-7 7-7-7"
              />
            </svg>
          </button>
          <div className="flex items-center space-x-2">
            <label className="text-sm">Sortuj po:</label>
            <select
              className="border rounded-md px-2 py-1"
              value={sortField}
              onChange={(e) => setSortField(e.target.value)}
            >
              <option value="orderDate">Data zamówienia</option>
              <option value="totalPrice">Wartość</option>
              <option value="orderStatus">Status</option>
            </select>
            <button
              onClick={() => setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc')}
              className="p-2 hover:bg-gray-100 rounded-md"
            >
              {sortDirection === 'asc' ? '↑' : '↓'}
            </button>
          </div>
        </div>
      </div>

      {isFiltersVisible && renderFilters()}

      <div className="space-y-4">
        {orders.length === 0 ? (
          <div className="text-center py-8 bg-gray-50 rounded-lg">
            <p className="text-gray-600">Nie masz jeszcze żadnych zamówień</p>
            <Button className="mt-4" onClick={() => navigate('/')}>
              Przejdź do sklepu
            </Button>
          </div>
        ) : (
          orders.map((order) => (
            <div
              key={order.id}
              className="border rounded-lg p-6 hover:border-gray-400 transition-colors cursor-pointer"
              onClick={() => navigate(`/customer/orders/${order.id}`)}
            >
              <div className="flex justify-between items-start mb-4">
                <div>
                  <div className="text-sm text-gray-500">
                    Zamówienie #{order.id} • {formatDate(order.orderDate)}
                  </div>
                  <div className="font-medium mt-1">{formatPrice(order.totalPrice)}</div>
                </div>
                <span
                  className={`px-3 py-1 rounded-full text-sm ${getStatusBadgeColor(order.orderStatus)}`}
                >
                  {translateStatus(order.orderStatus)}
                </span>
              </div>

              <div className="space-y-2">
                {order.orderItems.map((item) => (
                  <div key={item.id} className="flex justify-between text-sm">
                    <div>
                      <span className="font-medium">{item.productName}</span>
                      <span className="text-gray-500 ml-2">x{item.quantity}</span>
                    </div>
                    <span>{formatPrice(item.price * item.quantity)}</span>
                  </div>
                ))}
              </div>

              <div className="mt-4 pt-4 border-t text-sm text-gray-500">
                <div>Metoda płatności: {translatePaymentMethod(order.paymentMethod)}</div>
                <div>Metoda dostawy: {translateShippingMethod(order.shippingMethod)}</div>
                <div>
                  Adres: {order.shippingAddress.street}, {order.shippingAddress.zipCode}{' '}
                  {order.shippingAddress.city}
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {orders.length > 0 && (
        <div className="mt-6 flex justify-center space-x-2">
          <Button
            variant="outline"
            onClick={() => setCurrentPage((prev) => Math.max(0, prev - 1))}
            disabled={currentPage === 0}
          >
            Poprzednia
          </Button>
          <span className="py-2 px-4 border rounded-md">
            {currentPage + 1} z {totalPages}
          </span>
          <Button
            variant="outline"
            onClick={() => setCurrentPage((prev) => Math.min(totalPages - 1, prev + 1))}
            disabled={currentPage === totalPages - 1}
          >
            Następna
          </Button>
        </div>
      )}
    </div>
  );
};

export default OrderList;
