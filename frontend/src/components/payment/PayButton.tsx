import React, { useState } from 'react';
import { Button } from '../ui';
import { stripeService } from '../../services/stripeService';
import { PaymentStatus } from '../../types/stripe';

interface PayButtonProps {
  orderId: string;
  paymentStatus: PaymentStatus;
  className?: string;
}

export const PayButton: React.FC<PayButtonProps> = ({ orderId, paymentStatus, className = '' }) => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Only show the button for orders that are not paid
  if (paymentStatus === 'COMPLETED') {
    return null;
  }

  const handlePayment = async () => {
    if (!orderId) return;

    try {
      setIsLoading(true);
      setError(null);

      // Get the checkout session
      const sessionPromise = stripeService.createOrGetCheckoutSession(orderId);

      // Minimum delay of 1 second to show the animation
      await new Promise(resolve => setTimeout(resolve, 1000));

      // Now get the actual session
      const session = await sessionPromise;

      if (session && session.sessionUrl) {
        // Redirect to Stripe's checkout page
        window.location.href = session.sessionUrl;
      } else {
        throw new Error('No checkout URL available');
      }
    } catch (error) {
      console.error('Error getting checkout session:', error);
      setError('Nie można połączyć się z serwisem płatności. Spróbuj ponownie za chwilę.');
      setIsLoading(false);
    }
  };

  return (
    <div className={className}>
      {error && (
        <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 mb-3 text-sm">
          <p className="flex items-center">
            <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
              <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd"></path>
            </svg>
            {error}
          </p>
        </div>
      )}

      <Button
        onClick={handlePayment}
        disabled={isLoading}
        className="w-full"
        variant={paymentStatus === 'FAILED' || paymentStatus === 'CANCELED' ? 'primary' : 'outline'}
      >
        {isLoading ? (
          <span className="flex items-center justify-center">
            <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            Przygotowywanie płatności...
          </span>
        ) : (
          <span className="flex items-center justify-center">
            <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
              <path fillRule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clipRule="evenodd"></path>
            </svg>
            {paymentStatus === 'FAILED' || paymentStatus === 'CANCELED'
              ? 'Spróbuj zapłacić ponownie'
              : 'Zapłać teraz'}
          </span>
        )}
      </Button>
    </div>
  );
};
