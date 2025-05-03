import React, { useState, useEffect } from 'react';
import { Button } from '../ui';
import { stripeService } from '../../services/stripeService';

interface StripeCheckoutWrapperProps {
  orderId: string;
  onError?: (error: Error) => void;
  onSuccess?: () => void;
  disabled?: boolean;
}

// A simpler approach that redirects to Stripe's hosted checkout
export const StripeCheckoutWrapper: React.FC<StripeCheckoutWrapperProps> = ({
  orderId,
  onError,
  onSuccess,
  disabled,
}) => {
  const [isLoading, setIsLoading] = useState(false);

  const [sessionId, setSessionId] = useState<string | null>(null);

  // Create and get the session when the component mounts
  useEffect(() => {
    if (!orderId) return;

    const setupSession = async () => {
      try {
        // Create or get a checkout session
        const successUrl = `${window.location.origin}/checkout/success`;
        const cancelUrl = `${window.location.origin}/checkout/cancel`;

        const session = await stripeService.createOrGetCheckoutSession(orderId, successUrl, cancelUrl);

        if (session) {
          if (session.sessionUrl && session.sessionUrl.startsWith('http')) {
            // If we have a full URL, use it as the session ID
            setSessionId(session.sessionUrl);
          } else if (session.sessionId) {
            // Otherwise use the session ID
            setSessionId(session.sessionId);
          }
        }
      } catch (error) {
        console.error('Error setting up session:', error);
        onError?.(error instanceof Error ? error : new Error('Failed to set up payment session'));
      }
    };

    setupSession();
  }, [orderId, onError]);

  const handlePayment = () => {
    if (!orderId) return;

    try {
      setIsLoading(true);

      if (sessionId) {
        // If we have a session ID, redirect directly to the static HTML page
        localStorage.setItem('currentOrderId', orderId);

        // Check if the sessionId is a full URL
        if (sessionId.startsWith('http')) {
          // Redirect directly to the URL
          window.location.href = sessionId;
        } else {
          // Otherwise, use our static HTML page
          window.location.href = `/stripe-redirect.html?session_id=${sessionId}&order_id=${orderId}`;
        }
      } else {
        // Otherwise, redirect to our direct payment page which will handle getting the session
        window.location.href = `/payment/direct-stripe?orderId=${orderId}`;
      }

      // Note: onSuccess will not be called here since we're redirecting
      // It should be handled on the return page after payment
    } catch (error) {
      console.error('Error initiating payment:', error);
      onError?.(error instanceof Error ? error : new Error('Payment initiation failed'));
      setIsLoading(false);
    }
  };

  return (
    <Button
      onClick={handlePayment}
      disabled={disabled || isLoading}
      className="w-full"
    >
      {isLoading ? 'Processing...' : 'Pay with Stripe'}
    </Button>
  );
};
