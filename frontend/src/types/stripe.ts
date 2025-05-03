export type PaymentStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'CANCELED';

export interface StripeCheckoutSession {
  id: string;
  orderId: string;
  status: 'open' | 'complete' | 'expired';
  paymentStatus: 'pending' | 'paid' | 'failed';
}

export interface CreateCheckoutSessionRequest {
  orderId: string;
  items: Array<{
    productId: number;
    quantity: number;
    price: number;
    name: string;
  }>;
  customerEmail?: string;
  metadata?: Record<string, string>;
}

export interface CreateCheckoutSessionResponse {
  sessionId: string;
}

export interface PaymentStatusResponse {
  orderId: string;
  paymentStatus: PaymentStatus;
  sessionId?: string;
}