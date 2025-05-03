import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { stripeService } from '../../services/stripeService';
import { Button } from '../../components/ui';

const StripePayment: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [orderId, setOrderId] = useState<string | null>(null);

  // Parse query parameters to get orderId
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const id = params.get('orderId');
    if (id) {
      setOrderId(id);
    } else {
      setError('No order ID provided');
      setIsLoading(false);
    }
  }, [location.search]);

  // Redirect to Stripe when orderId is available
  useEffect(() => {
    const redirectToStripe = async () => {
      if (!orderId) return;

      try {
        setIsLoading(true);

        // Create or get a checkout session
        const successUrl = `${window.location.origin}/checkout/success`;
        const cancelUrl = `${window.location.origin}/checkout/cancel`;

        const session = await stripeService.createOrGetCheckoutSession(orderId, successUrl, cancelUrl);

        if (!session || !session.sessionUrl) {
          throw new Error('Failed to get checkout session');
        }

        // Store the order ID in local storage for retrieval after redirect
        localStorage.setItem('currentOrderId', orderId);

        // Redirect directly to Stripe's checkout page
        window.location.href = session.sessionUrl;
      } catch (err) {
        console.error('Error redirecting to Stripe:', err);
        setError('Failed to initialize payment. Please try again.');
        setIsLoading(false);
      }
    };

    redirectToStripe();
  }, [orderId]);

  const handleCancel = () => {
    if (orderId) {
      navigate(`/payment/confirmation?orderId=${orderId}&status=canceled`);
    } else {
      navigate('/customer/checkout');
    }
  };

  if (isLoading) {
    return (
      <div className="container mx-auto px-4 py-16 text-center">
        <div className="max-w-md mx-auto bg-white p-8 rounded-lg shadow-md">
          <h1 className="text-2xl font-semibold mb-4">Redirecting to Stripe...</h1>
          <div className="flex justify-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900"></div>
          </div>
          <p className="mt-4 text-gray-600">Please wait while we prepare your payment...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto px-4 py-16 text-center">
        <div className="max-w-md mx-auto bg-white p-8 rounded-lg shadow-md">
          <h1 className="text-2xl font-semibold mb-4">Payment Error</h1>
          <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 mb-6">
            {error}
          </div>
          <Button onClick={handleCancel}>Return to Checkout</Button>
        </div>
      </div>
    );
  }

  return null;
};

export default StripePayment;
