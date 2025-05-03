import { loadStripe } from '@stripe/stripe-js';

// Get the Stripe public key from environment variables
const STRIPE_PUBLIC_KEY = process.env.REACT_APP_STRIPE_PUBLIC_KEY;

// Log the key for debugging (remove in production)
console.log('Stripe Public Key:', STRIPE_PUBLIC_KEY ? 'Key exists' : 'Key missing');

// Ensure the public key is available
if (!STRIPE_PUBLIC_KEY) {
  console.error('Missing Stripe public key in environment variables');
}

// Create a singleton instance of Stripe
export const getStripe = (() => {
  let stripePromise: Promise<any> | null = null;

  return () => {
    if (!stripePromise && STRIPE_PUBLIC_KEY) {
      stripePromise = loadStripe(STRIPE_PUBLIC_KEY);
    }
    return stripePromise;
  };
})();

// Make stripePromise available for CheckoutProvider
export const stripePromise = STRIPE_PUBLIC_KEY ? loadStripe(STRIPE_PUBLIC_KEY) : null;

// Stripe Checkout configuration
export const STRIPE_CONFIG = {
  successUrl: `${window.location.origin}/checkout/success`,
  cancelUrl: `${window.location.origin}/checkout/cancel`,
  mode: 'payment' as const,
} as const;
