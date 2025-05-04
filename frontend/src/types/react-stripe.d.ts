declare module '@stripe/react-stripe-js' {
  import { Stripe } from '@stripe/stripe-js';
  import React from 'react';

  export interface ElementsContextValue {
    elements: any;
    stripe: Stripe | null;
  }

  export const Elements: React.FC<{
    stripe: Promise<Stripe | null>;
    options?: any;
    children: React.ReactNode;
  }>;

  export const useStripe: () => Stripe | null;
  export const useElements: () => any;
}
