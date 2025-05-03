import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { stripeService } from '../../services/stripeService';
import { Button } from '../../components/ui';

const DirectStripePayment: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [orderId, setOrderId] = useState<string | null>(null);
  const [checkoutUrl, setCheckoutUrl] = useState<string | null>(null);

  // Parse query parameters to get orderId
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const id = params.get('orderId');
    if (id) {
      setOrderId(id);
      getCheckoutUrl(id);
    } else {
      setError('No order ID provided');
      setIsLoading(false);
    }
  }, [location.search]);

  // Get the checkout URL from the backend
  const getCheckoutUrl = async (id: string) => {
    try {
      setIsLoading(true);

      // Create or get a checkout session
      const successUrl = `${window.location.origin}/checkout/success`;
      const cancelUrl = `${window.location.origin}/checkout/cancel`;

      const session = await stripeService.createOrGetCheckoutSession(id, successUrl, cancelUrl);

      if (session && session.sessionUrl) {
        setCheckoutUrl(session.sessionUrl);

        // Store the order ID in local storage for retrieval after redirect
        localStorage.setItem('currentOrderId', id);

        // Log the URL for debugging
        console.log('Checkout URL:', session.sessionUrl);

        // Don't automatically redirect - let the user click the button
        // This gives them a chance to see the URL and helps with debugging
      } else {
        throw new Error('No checkout URL available');
      }
    } catch (err) {
      console.error('Error getting checkout URL:', err);
      setError('Failed to get checkout URL. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleManualRedirect = () => {
    if (checkoutUrl) {
      window.location.href = checkoutUrl;
    }
  };

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
          <h1 className="text-2xl font-semibold mb-4">Preparing Your Payment</h1>
          <div className="flex justify-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900"></div>
          </div>
          <p className="mt-4 text-gray-600">Please wait while we connect to Stripe...</p>
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

  if (checkoutUrl) {
    // Extract the session ID from the URL or use the full URL
    const sessionId = checkoutUrl.startsWith('http') ? checkoutUrl : checkoutUrl.split('/').pop() || '';

    return (
      <div className="container mx-auto px-4 py-16 text-center">
        <div className="max-w-md mx-auto bg-white p-8 rounded-lg shadow-md">
          <h1 className="text-2xl font-semibold mb-4">Redirecting to Stripe</h1>
          <p className="mb-6">You are being redirected to Stripe's secure payment page.</p>
          <p className="mb-6">If you are not redirected automatically, please click the button below:</p>

          <div className="mb-4 p-4 bg-gray-50 border border-gray-200 rounded-lg">
            <p className="text-sm text-gray-700 break-all">
              <strong>Debug Info:</strong><br />
              Session ID: {sessionId}<br />
              Full URL: {checkoutUrl}
            </p>
          </div>

          <div className="flex flex-col space-y-4">
            {/* Direct link button */}
            <Button onClick={handleManualRedirect}>
              Continue to Payment (Direct Link)
            </Button>

            {/* Static HTML page redirect */}
            <a href={`/stripe-redirect.html?session_id=${sessionId}&order_id=${orderId}`} className="w-full">
              <Button className="w-full">
                Continue to Payment (Static HTML)
              </Button>
            </a>

            <Button variant="outline" onClick={handleCancel}>
              Cancel
            </Button>
          </div>
        </div>
      </div>
    );
  }

  return null;
};

export default DirectStripePayment;
