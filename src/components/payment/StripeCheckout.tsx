import React, { useState, useEffect } from 'react';
import { stripeService } from '../../services/stripeService';
import { Button } from '../ui';
import type { CreateCheckoutSessionRequest, PaymentStatus } from '../../types/stripe';

interface StripeCheckoutProps {
  orderId: string;
  items: Array<{
    productId: number;
    quantity: number;
    price: number;
    name: string;
  }>;
  customerEmail?: string;
  onError?: (error: Error) => void;
  onSuccess?: () => void;
  onStatusChange?: (status: PaymentStatus) => void;
  disabled?: boolean;
}

export const StripeCheckout: React.FC<StripeCheckoutProps> = ({
  orderId,
  items,
  customerEmail,
  onError,
  onSuccess,
  onStatusChange,
  disabled = false,
}) => {
  const [isLoading, setIsLoading] = useState(false);
  const [paymentStatus, setPaymentStatus] = useState<PaymentStatus | null>(null);
  // No longer tracking paymentIntentId as it's been removed from the backend

  // Poll for payment status updates
  useEffect(() => {
    let intervalId: NodeJS.Timeout;

    const checkPaymentStatus = async () => {
      if (!orderId) return;

      try {
        const statusResponse = await stripeService.getPaymentStatus(orderId);
        setPaymentStatus(statusResponse.paymentStatus);

        // No longer setting paymentIntentId as it's been removed from the backend

        // Notify parent component of status change
        onStatusChange?.(statusResponse.paymentStatus);

        // If payment is complete or failed, stop polling
        if (['COMPLETED', 'FAILED', 'CANCELED'].includes(statusResponse.paymentStatus)) {
          clearInterval(intervalId);

          if (statusResponse.paymentStatus === 'COMPLETED') {
            onSuccess?.();
          } else if (statusResponse.paymentStatus === 'FAILED') {
            onError?.(new Error('Payment failed'));
          }
        }
      } catch (error) {
        console.error('Error checking payment status:', error);
      }
    };

    // Start polling when component mounts
    intervalId = setInterval(checkPaymentStatus, 3000); // Check every 3 seconds

    // Initial check
    checkPaymentStatus();

    return () => {
      clearInterval(intervalId);
    };
  }, [orderId, onStatusChange, onSuccess, onError]);

  const handleInitiatePayment = async () => {
    try {
      setIsLoading(true);

      // Initiate payment processing
      await stripeService.initiatePayment(orderId);

      // After initiating, start checking for status updates
      const session = await stripeService.createOrGetCheckoutSession(orderId);
      setPaymentStatus(session.paymentStatus);

      // No longer setting paymentIntentId as it's been removed from the backend

      onStatusChange?.(session.paymentStatus);
    } catch (error) {
      console.error('Stripe payment initiation error:', error);
      onError?.(error instanceof Error ? error : new Error('Payment initiation failed'));
    } finally {
      setIsLoading(false);
    }
  };

  const handleVerifyPayment = async () => {
    if (!orderId) return;

    try {
      setIsLoading(true);
      // Check the payment status using the checkout session
      const session = await stripeService.createOrGetCheckoutSession(orderId);
      setPaymentStatus(session.paymentStatus);
      onStatusChange?.(session.paymentStatus);
    } catch (error) {
      console.error('Payment verification error:', error);
      onError?.(error instanceof Error ? error : new Error('Payment verification failed'));
    } finally {
      setIsLoading(false);
    }
  };

  const handleCancelPayment = async () => {
    if (!orderId) return;

    try {
      setIsLoading(true);
      await stripeService.cancelPayment(orderId);
      // After cancellation request, the status will be updated via polling
    } catch (error) {
      console.error('Payment cancellation error:', error);
      onError?.(error instanceof Error ? error : new Error('Payment cancellation failed'));
    } finally {
      setIsLoading(false);
    }
  };

  // Legacy checkout method (keeping for compatibility)
  const handleCheckout = async () => {
    try {
      setIsLoading(true);

      const checkoutRequest: CreateCheckoutSessionRequest = {
        orderId,
        items,
        customerEmail,
      };

      // Create checkout session
      const { sessionId } = await stripeService.createLegacyCheckoutSession(checkoutRequest);

      // Redirect to Stripe Checkout
      await stripeService.redirectToCheckout(sessionId);

      onSuccess?.();
    } catch (error) {
      console.error('Stripe checkout error:', error);
      onError?.(error instanceof Error ? error : new Error('Payment failed'));
    } finally {
      setIsLoading(false);
    }
  };

  // Render payment status information
  const renderPaymentStatus = () => {
    if (paymentStatus === 'PROCESSING') {
      return (
        <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg text-blue-700 text-center mb-4">
          <p className="font-medium">Payment is being processed</p>
          <p className="text-sm mt-1">Please wait while we confirm your payment...</p>
        </div>
      );
    }

    if (paymentStatus === 'COMPLETED') {
      return (
        <div className="p-4 bg-green-50 border border-green-200 rounded-lg text-green-700 text-center mb-4">
          <p className="font-medium">Payment completed successfully</p>
          <p className="text-sm mt-1">Thank you for your purchase!</p>
        </div>
      );
    }

    if (paymentStatus === 'FAILED') {
      return (
        <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 text-center mb-4">
          <p className="font-medium">Payment failed</p>
          <p className="text-sm mt-1">There was a problem processing your payment.</p>
        </div>
      );
    }

    if (paymentStatus === 'CANCELED') {
      return (
        <div className="p-4 bg-yellow-50 border border-yellow-200 rounded-lg text-yellow-700 text-center mb-4">
          <p className="font-medium">Payment canceled</p>
          <p className="text-sm mt-1">Your payment has been canceled.</p>
        </div>
      );
    }

    return null;
  };

  // Render action buttons based on payment status
  const renderActionButtons = () => {
    // Only show action buttons on the confirmation page
    if (!window.location.pathname.includes('/confirmation')) {
      return null;
    }

    if (paymentStatus === 'PROCESSING') {
      return (
        <div className="space-y-2">
          <Button
            onClick={handleVerifyPayment}
            disabled={disabled || isLoading}
            className="w-full"
          >
            {isLoading ? 'Verifying...' : 'Verify Payment Status'}
          </Button>
          <Button
            onClick={handleCancelPayment}
            disabled={disabled || isLoading}
            variant="outline"
            className="w-full"
          >
            Cancel Payment
          </Button>
        </div>
      );
    }

    if (paymentStatus === 'FAILED' || paymentStatus === 'CANCELED') {
      return (
        <Button
          onClick={handleInitiatePayment}
          disabled={disabled || isLoading}
          className="w-full"
        >
          Try Again
        </Button>
      );
    }

    return null;
  };

  return (
    <div className="space-y-4">
      {renderPaymentStatus()}
      {renderActionButtons()}
    </div>
  );
};