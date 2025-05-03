import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { stripeService } from '../../services/stripeService';
import { Button } from '../../components/ui';
import { PaymentStatus } from '../../types/stripe';

const PaymentConfirmation: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [isLoading, setIsLoading] = useState(true);
  const [paymentStatus, setPaymentStatus] = useState<PaymentStatus | null>(null);
  const [orderId, setOrderId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Parse query parameters and handle different return paths
  useEffect(() => {
    const handlePaymentReturn = async () => {
      // Check if we're on a success or cancel route
      const isSuccess = location.pathname.includes('/checkout/success');
      const isCancel = location.pathname.includes('/checkout/cancel');

      // Get query parameters
      const params = new URLSearchParams(location.search);
      let id = params.get('orderId');
      const status = params.get('status');

      // For Stripe return URLs, the order ID might be in a different format
      if (!id && params.has('session_id')) {
        // We need to find the order ID from the session ID
        // This would require a backend endpoint, but for now we'll use local storage
        id = localStorage.getItem('currentOrderId');
      }

      if (id) {
        setOrderId(id);

        // If we're returning from Stripe success/cancel routes
        if (isSuccess) {
          setPaymentStatus('COMPLETED');
          setIsLoading(false);
        } else if (isCancel) {
          setPaymentStatus('CANCELED');
          setIsLoading(false);
        }
        // If status is provided in URL, use it
        else if (status === 'success') {
          setPaymentStatus('COMPLETED');
          setIsLoading(false);
        } else if (status === 'canceled') {
          setPaymentStatus('CANCELED');
          setIsLoading(false);
        } else {
          // Otherwise, check payment status from API
          await checkPaymentStatus(id);
        }
      } else {
        setError('No order ID provided');
        setIsLoading(false);
      }
    };

    handlePaymentReturn();
  }, [location.pathname, location.search]);

  // Check payment status from API
  const checkPaymentStatus = async (id: string) => {
    try {
      setIsLoading(true);

      // Get the checkout session
      try {
        const session = await stripeService.createOrGetCheckoutSession(id);
        setPaymentStatus(session.paymentStatus);
      } catch (sessionError) {
        console.error('Failed to get checkout session:', sessionError);
        throw sessionError;
      }
    } catch (err) {
      console.error('Error checking payment status:', err);
      setError('Failed to check payment status');
    } finally {
      setIsLoading(false);
    }
  };

  const handleContinueShopping = () => {
    navigate('/');
  };

  const handleViewOrders = () => {
    navigate('/customer/orders');
  };

  const handleTryAgain = () => {
    if (orderId) {
      navigate(`/payment/stripe?orderId=${orderId}`);
    } else {
      navigate('/customer/checkout');
    }
  };

  if (isLoading) {
    return (
      <div className="container mx-auto px-4 py-16 text-center">
        <div className="max-w-md mx-auto bg-white p-8 rounded-lg shadow-md">
          <h1 className="text-2xl font-semibold mb-4">Checking Payment Status...</h1>
          <div className="flex justify-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900"></div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-16">
      <div className="max-w-md mx-auto bg-white p-8 rounded-lg shadow-md">
        <h1 className="text-2xl font-semibold mb-6 text-center">Payment {getStatusText()}</h1>

        {error && (
          <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 mb-6">
            {error}
          </div>
        )}

        <div className={`p-4 rounded-lg mb-6 ${getStatusBgColor()}`}>
          <p className={getStatusTextColor()}>
            Order ID: <span className="font-semibold">{orderId}</span>
          </p>
          <p className={`${getStatusTextColor()} mt-2`}>
            Status: <span className="font-semibold">{getStatusText()}</span>
          </p>
          {getStatusMessage()}
        </div>

        <div className="flex flex-col space-y-3">
          {(paymentStatus === 'FAILED' || paymentStatus === 'CANCELED') && (
            <Button onClick={handleTryAgain}>Try Again</Button>
          )}

          <Button onClick={handleViewOrders} variant="outline">
            View My Orders
          </Button>

          <Button onClick={handleContinueShopping} variant="outline">
            Continue Shopping
          </Button>
        </div>
      </div>
    </div>
  );

  // Helper functions for UI
  function getStatusText() {
    switch (paymentStatus) {
      case 'COMPLETED': return 'Successful';
      case 'PROCESSING': return 'Processing';
      case 'FAILED': return 'Failed';
      case 'CANCELED': return 'Canceled';
      default: return 'Pending';
    }
  }

  function getStatusBgColor() {
    switch (paymentStatus) {
      case 'COMPLETED': return 'bg-green-50 border border-green-200';
      case 'PROCESSING': return 'bg-blue-50 border border-blue-200';
      case 'FAILED': return 'bg-red-50 border border-red-200';
      case 'CANCELED': return 'bg-yellow-50 border border-yellow-200';
      default: return 'bg-gray-50 border border-gray-200';
    }
  }

  function getStatusTextColor() {
    switch (paymentStatus) {
      case 'COMPLETED': return 'text-green-700';
      case 'PROCESSING': return 'text-blue-700';
      case 'FAILED': return 'text-red-700';
      case 'CANCELED': return 'text-yellow-700';
      default: return 'text-gray-700';
    }
  }

  function getStatusMessage() {
    switch (paymentStatus) {
      case 'COMPLETED':
        return <p className="text-green-700 mt-2">Thank you for your purchase!</p>;
      case 'PROCESSING':
        return <p className="text-blue-700 mt-2">Your payment is being processed. Please check back later.</p>;
      case 'FAILED':
        return <p className="text-red-700 mt-2">Your payment could not be processed. Please try again.</p>;
      case 'CANCELED':
        return <p className="text-yellow-700 mt-2">Your payment was canceled. You can try again if you wish.</p>;
      default:
        return <p className="text-gray-700 mt-2">Waiting for payment confirmation.</p>;
    }
  }
};

export default PaymentConfirmation;
