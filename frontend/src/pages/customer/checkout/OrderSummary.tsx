import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../contexts/AuthContext';
import { useCartContext } from '../../../contexts/CartContext';
import { Input, Button } from '../../../components/ui';

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

interface ShippingFormData {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  street: string;
  city: string;
  postalCode: string;
  country: string;
  companyName?: string;
  nip?: string;
}

type CheckoutStep = 'summary' | 'shipping' | 'payment' | 'confirmation';

const SHIPPING_METHODS = [
  {
    id: ShippingMethod.INPOST,
    name: 'InPost',
    price: 14.99,
    time: '2-3 dni robocze',
    icon: '📦',
  },
  {
    id: ShippingMethod.DHL,
    name: 'DHL',
    price: 19.99,
    time: 'następny dzień roboczy',
    icon: '🚚',
  },
];

const PAYMENT_METHODS = [
  { id: PaymentMethod.CREDIT_CARD, name: 'Karta płatnicza', icon: '💳' },
  { id: PaymentMethod.BLIK, name: 'BLIK', icon: '📱' },
  { id: PaymentMethod.BANK_TRANSFER, name: 'Przelew bankowy', icon: '🏦' },
  { id: PaymentMethod.PAYPAL, name: 'PayPal', icon: '🌐' },
];

const OrderSummary: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { cart } = useCartContext();
  const [currentStep, setCurrentStep] = useState<CheckoutStep>('summary');
  const [formData, setFormData] = useState<ShippingFormData>({
    firstName: '',
    lastName: '',
    email: user?.email || '',
    phone: '',
    street: '',
    city: '',
    postalCode: '',
    country: 'Polska',
  });
  const [wantInvoice, setWantInvoice] = useState(false);
  const [selectedShipping, setSelectedShipping] = useState<ShippingMethod>(ShippingMethod.INPOST);
  const [selectedPayment, setSelectedPayment] = useState<PaymentMethod>(PaymentMethod.CREDIT_CARD);
  const [orderId, setOrderId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user?.id || !cart) return;

    setError(null);
    setIsSubmitting(true);

    try {
      const orderData = {
        shippingAddress: {
          firstName: formData.firstName,
          lastName: formData.lastName,
          email: formData.email,
          phone: formData.phone,
          street: formData.street,
          city: formData.city,
          zipCode: formData.postalCode,
          country: formData.country,
          companyName: formData.companyName,
          nip: formData.nip,
        },
        paymentMethod: selectedPayment,
        shippingMethod: selectedShipping,
        cart: cart,
      };

      const response = await fetch(`http://localhost:8080/api/v1/users/${user.id}/orders`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
        body: JSON.stringify(orderData),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => null);
        throw new Error(errorData?.message || 'Nie udało się utworzyć zamówienia');
      }

      const data = await response.json();
      setOrderId(data.id);
      setCurrentStep('confirmation');
    } catch (error) {
      console.error('Error creating order:', error);
      setError(
        error instanceof Error
          ? error.message
          : 'Nie udało się utworzyć zamówienia. Spróbuj ponownie.'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  const calculateTotal = () => {
    if (!cart?.cartItems) return 0;
    const itemsTotal = cart.cartItems.reduce(
      (total, item) => total + item.price * item.quantity,
      0
    );
    const shippingCost = SHIPPING_METHODS.find((m) => m.id === selectedShipping)?.price ?? 0;
    return itemsTotal + shippingCost;
  };

  const getShippingPrice = (method: ShippingMethod) => {
    return SHIPPING_METHODS.find((m) => m.id === method)?.price ?? 0;
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('pl-PL', {
      style: 'currency',
      currency: 'PLN',
      minimumFractionDigits: 2,
    }).format(price);
  };

  const renderStepIndicator = () => (
    <div className="mb-8">
      <div className="flex items-center justify-center space-x-4">
        {['Koszyk', 'Dane dostawy', 'Metoda wysyłki', 'Płatność', 'Potwierdzenie'].map(
          (step, index) => (
            <React.Fragment key={step}>
              <div className="flex items-center">
                <div
                  className={`w-8 h-8 rounded-full flex items-center justify-center ${
                    index <= getStepNumber() ? 'bg-black text-white' : 'bg-gray-200'
                  }`}
                >
                  {index + 1}
                </div>
                <span className="ml-2">{step}</span>
              </div>
              {index < 4 && (
                <div
                  className={`w-16 h-0.5 ${index < getStepNumber() ? 'bg-black' : 'bg-gray-200'}`}
                />
              )}
            </React.Fragment>
          )
        )}
      </div>
    </div>
  );

  const getStepNumber = () => {
    switch (currentStep) {
      case 'summary':
        return 0;
      case 'shipping':
        return 1;
      case 'payment':
        return 2;
      case 'confirmation':
        return 3;
      default:
        return 0;
    }
  };

  const renderSummaryStep = () => (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
      <div>
        <h2 className="text-2xl font-semibold mb-6">Dane do wysyłki</h2>
        <form className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Imię"
              name="firstName"
              value={formData.firstName}
              onChange={handleInputChange}
              required
            />
            <Input
              label="Nazwisko"
              name="lastName"
              value={formData.lastName}
              onChange={handleInputChange}
              required
            />
          </div>
          <Input
            label="Email"
            type="email"
            name="email"
            value={formData.email}
            onChange={handleInputChange}
            required
          />
          <Input
            label="Telefon"
            type="tel"
            name="phone"
            value={formData.phone}
            onChange={handleInputChange}
            required
          />
          <Input
            label="Adres"
            name="street"
            value={formData.street}
            onChange={handleInputChange}
            required
          />
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Miasto"
              name="city"
              value={formData.city}
              onChange={handleInputChange}
              required
            />
            <Input
              label="Kod pocztowy"
              name="postalCode"
              value={formData.postalCode}
              onChange={handleInputChange}
              required
            />
          </div>
          <Input
            label="Kraj"
            name="country"
            value={formData.country}
            onChange={handleInputChange}
            required
          />

          <div className="mt-6">
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={wantInvoice}
                onChange={(e) => setWantInvoice(e.target.checked)}
                className="rounded border-gray-300 text-black focus:ring-black"
              />
              <span className="ml-2">Chcę otrzymać fakturę</span>
            </label>
          </div>

          {wantInvoice && (
            <div className="space-y-4 mt-4">
              <Input
                label="Nazwa firmy"
                name="companyName"
                value={formData.companyName || ''}
                onChange={handleInputChange}
              />
              <Input
                label="NIP"
                name="nip"
                value={formData.nip || ''}
                onChange={handleInputChange}
              />
            </div>
          )}
        </form>
      </div>

      <div>
        <div className="bg-gray-50 p-6 rounded-lg">
          <h2 className="text-2xl font-semibold mb-6">Podsumowanie zamówienia</h2>
          <div className="space-y-4">
            {cart?.cartItems?.map((item) => (
              <div key={item.id} className="flex justify-between">
                <div>
                  <span className="font-medium">{item.productName}</span>
                  <span className="text-gray-500 ml-2">x{item.quantity}</span>
                </div>
                <span>{formatPrice(item.price * item.quantity)}</span>
              </div>
            ))}
            <div className="border-t pt-4 mt-4">
              <div className="flex justify-between">
                <span>Wysyłka</span>
                <span>
                  {selectedShipping === ShippingMethod.DHL ? formatPrice(19.99) : 'GRATIS'}
                </span>
              </div>
              <div className="flex justify-between font-semibold text-lg mt-4">
                <span>Suma</span>
                <span>{formatPrice(calculateTotal())}</span>
              </div>
            </div>
          </div>

          <Button
            className="w-full mt-6"
            onClick={() => setCurrentStep('shipping')}
            disabled={!formData.firstName || !formData.email || !formData.phone || !formData.street}
          >
            Dalej
          </Button>
        </div>
      </div>
    </div>
  );

  const renderShippingStep = () => (
    <div className="max-w-2xl mx-auto">
      <h2 className="text-2xl font-semibold mb-6">Wybierz metodę dostawy</h2>
      <div className="space-y-4">
        {SHIPPING_METHODS.map((method) => (
          <label
            key={method.id}
            className={`block p-4 border rounded-lg cursor-pointer transition-colors ${
              selectedShipping === method.id
                ? 'border-black bg-gray-50'
                : 'border-gray-200 hover:border-gray-300'
            }`}
          >
            <div className="flex items-center">
              <input
                type="radio"
                name="shipping"
                value={method.id}
                checked={selectedShipping === method.id}
                onChange={(e) => setSelectedShipping(e.target.value as ShippingMethod)}
                className="text-black focus:ring-black"
              />
              <div className="ml-3 flex items-center">
                <span className="mr-2">{method.icon}</span>
                <div>
                  <div className="font-medium">{method.name}</div>
                  <div className="text-sm text-gray-500">
                    {formatPrice(method.price)} • {method.time}
                  </div>
                </div>
              </div>
            </div>
          </label>
        ))}
      </div>

      <div className="mt-8 flex justify-between">
        <Button variant="outline" onClick={() => setCurrentStep('summary')}>
          Wstecz
        </Button>
        <Button onClick={() => setCurrentStep('payment')}>Dalej</Button>
      </div>
    </div>
  );

  const renderPaymentStep = () => (
    <div className="max-w-2xl mx-auto">
      <h2 className="text-2xl font-semibold mb-6">Wybierz metodę płatności</h2>

      {error && (
        <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700">
          {error}
        </div>
      )}

      <div className="space-y-4">
        {PAYMENT_METHODS.map((method) => (
          <label
            key={method.id}
            className={`block p-4 border rounded-lg cursor-pointer transition-colors ${
              selectedPayment === method.id
                ? 'border-black bg-gray-50'
                : 'border-gray-200 hover:border-gray-300'
            }`}
          >
            <div className="flex items-center">
              <input
                type="radio"
                name="payment"
                value={method.id}
                checked={selectedPayment === method.id}
                onChange={(e) => setSelectedPayment(e.target.value as PaymentMethod)}
                className="text-black focus:ring-black"
              />
              <div className="ml-3 flex items-center">
                <span className="mr-2">{method.icon}</span>
                <span className="font-medium">{method.name}</span>
              </div>
            </div>
          </label>
        ))}
      </div>

      <div className="mt-8 flex justify-between">
        <Button variant="outline" onClick={() => setCurrentStep('shipping')}>
          Wstecz
        </Button>
        <Button onClick={handleSubmit} disabled={isSubmitting}>
          {isSubmitting ? 'Przetwarzanie...' : 'Zamawiam i płacę'}
        </Button>
      </div>

      <div className="mt-6 p-4 bg-gray-50 rounded-lg">
        <div className="text-lg font-semibold mb-2">Podsumowanie</div>
        <div className="space-y-2">
          <div className="flex justify-between">
            <span>Wartość produktów</span>
            <span>{formatPrice(calculateTotal() - getShippingPrice(selectedShipping))}</span>
          </div>
          <div className="flex justify-between">
            <span>Koszt dostawy</span>
            <span>{formatPrice(getShippingPrice(selectedShipping))}</span>
          </div>
          <div className="flex justify-between font-semibold text-lg pt-2 border-t">
            <span>Łącznie do zapłaty</span>
            <span>{formatPrice(calculateTotal())}</span>
          </div>
        </div>
      </div>
    </div>
  );

  const renderConfirmationStep = () => (
    <div className="max-w-2xl mx-auto text-center">
      <div className="mb-8">
        <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <svg
            className="w-8 h-8 text-green-500"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
          </svg>
        </div>
        <h2 className="text-2xl font-semibold mb-2">Zamówienie zostało złożone!</h2>
        <p className="text-gray-600">Numer zamówienia: {orderId}</p>
      </div>

      <div className="bg-gray-50 p-6 rounded-lg mb-8 text-left">
        <h3 className="font-semibold mb-4">Podsumowanie zamówienia</h3>
        <div className="space-y-4">
          {cart?.cartItems?.map((item) => (
            <div key={item.id} className="flex justify-between">
              <div>
                <span className="font-medium">{item.productName}</span>
                <span className="text-gray-500 ml-2">x{item.quantity}</span>
              </div>
              <span>{formatPrice(item.price * item.quantity)}</span>
            </div>
          ))}
          <div className="border-t pt-4">
            <div className="flex justify-between">
              <span>Wysyłka</span>
              <span>{selectedShipping === ShippingMethod.DHL ? formatPrice(19.99) : 'GRATIS'}</span>
            </div>
            <div className="flex justify-between font-semibold text-lg mt-4">
              <span>Suma</span>
              <span>{formatPrice(calculateTotal())}</span>
            </div>
          </div>
        </div>
      </div>

      <div className="bg-yellow-50 p-4 rounded-lg mb-8">
        <p className="text-yellow-800">
          Status płatności: <span className="font-semibold">Oczekująca</span>
        </p>
        <p className="text-sm mt-2">Link do płatności zostanie wkrótce udostępniony.</p>
      </div>

      <div className="flex justify-center space-x-4">
        <Button variant="outline" onClick={() => navigate('/customer/orders')}>
          Moje zamówienia
        </Button>
        <Button onClick={() => navigate('/')}>Kontynuuj zakupy</Button>
      </div>
    </div>
  );

  return (
    <div className="container mx-auto px-4 py-8">
      {renderStepIndicator()}

      {currentStep === 'summary' && renderSummaryStep()}
      {currentStep === 'shipping' && renderShippingStep()}
      {currentStep === 'payment' && renderPaymentStep()}
      {currentStep === 'confirmation' && renderConfirmationStep()}
    </div>
  );
};

export default OrderSummary;
