
import api from './api';
import { getStripe } from '../config/stripe';
import type {
  CreateCheckoutSessionRequest,
  CreateCheckoutSessionResponse,
  StripeCheckoutSession,
  PaymentStatus
} from '../types/stripe';

export class StripeService {
  private static instance: StripeService;

  private constructor() {}

  static getInstance(): StripeService {
    if (!StripeService.instance) {
      StripeService.instance = new StripeService();
    }
    return StripeService.instance;
  }

  /**
   * Legacy method - creates a checkout session with the old API
   */
  async createLegacyCheckoutSession(request: CreateCheckoutSessionRequest): Promise<CreateCheckoutSessionResponse> {
    const response = await api.post<CreateCheckoutSessionResponse>(
      '/api/v1/payments/checkout-session',
      request
    );
    return response.data;
  }

  async redirectToCheckout(sessionId: string): Promise<void> {
    const stripe = await getStripe();
    const { error } = await stripe.redirectToCheckout({
      sessionId,
    });

    if (error) {
      throw new Error(error.message);
    }
  }

  async getSessionStatus(sessionId: string): Promise<StripeCheckoutSession> {
    const response = await api.get<StripeCheckoutSession>(
      `/api/v1/payments/checkout-session/${sessionId}`
    );
    return response.data;
  }

  /**
   * Initiates payment processing for an order
   * @param orderId The order ID to process payment for
   */
  async initiatePayment(orderId: string): Promise<void> {
    await api.post(`/api/v1/stripe/payment/initiate/${orderId}`);
  }

  /**
   * Creates or gets a checkout session for an order
   * @param orderId The order ID
   * @param successUrl The URL to redirect to on successful payment
   * @param cancelUrl The URL to redirect to on canceled payment
   * @returns The checkout session data including the session URL and expiration time
   */
  async createOrGetCheckoutSession(
    orderId: string,
    successUrl: string = `${window.location.origin}/checkout/success`,
    cancelUrl: string = `${window.location.origin}/checkout/cancel`
  ): Promise<{
    orderId: number;
    sessionId: string;
    sessionUrl: string;
    expiresAt: string;
    paymentStatus: PaymentStatus;
  }> {
    try {
      const request = {
        successUrl,
        cancelUrl
      };

      // Create or get the checkout session
      const response = await api.post(`/api/v1/stripe/checkout/session/${orderId}`, request);

      // Log the response for debugging
      console.log('Checkout session response:', response.data);

      // Ensure the sessionUrl is properly formatted
      const data = response.data;
      if (data && data.sessionId && !data.sessionUrl) {
        // If sessionUrl is missing but we have sessionId, construct it
        data.sessionUrl = `https://checkout.stripe.com/c/pay/${data.sessionId}`;
      }

      return data;
    } catch (error) {
      console.error('Error creating or getting checkout session:', error);
      throw error;
    }
  }

  /**
   * Legacy method - gets the checkout session for an order
   * @param orderId The order ID
   * @returns The checkout session data including the session URL
   * @deprecated Use createOrGetCheckoutSession instead
   */
  async getCheckoutSession(orderId: string): Promise<{
    orderId: number;
    sessionId: string;
    sessionUrl: string;
    paymentStatus: PaymentStatus;
  }> {
    return this.createOrGetCheckoutSession(orderId);
  }

  /**
   * Legacy method - creates a checkout session for an order
   * @param orderId The order ID
   * @param successUrl The URL to redirect to on successful payment
   * @param cancelUrl The URL to redirect to on canceled payment
   * @returns A promise that resolves with the session data
   * @deprecated Use createOrGetCheckoutSession instead
   */
  async createCheckoutSession(
    orderId: string,
    successUrl: string = `${window.location.origin}/checkout/success`,
    cancelUrl: string = `${window.location.origin}/checkout/cancel`
  ): Promise<{
    orderId: number;
    sessionId: string;
    sessionUrl: string;
    paymentStatus: PaymentStatus;
  }> {
    return this.createOrGetCheckoutSession(orderId, successUrl, cancelUrl);
  }

  /**
   * Gets the payment status for an order
   * @param orderId The order ID
   * @returns Payment status information
   */
  async getPaymentStatus(orderId: string): Promise<{
    orderId: number;
    paymentStatus: PaymentStatus;
    paymentTransactionId?: string;
    lastUpdated?: string;
  }> {
    try {
      const response = await api.get(`/api/v1/stripe/checkout/session/${orderId}`);
      return response.data;
    } catch (error) {
      console.error('Error getting payment status:', error);
      throw error;
    }
  }

  /**
   * Cancels a payment for an order
   * @param orderId The order ID
   * @returns Response message
   */
  async cancelPayment(orderId: string): Promise<{ message: string }> {
    const response = await api.post(`/api/v1/stripe/payment/cancel/${orderId}`);
    return response.data;
  }

  /**
   * Verifies a payment status
   * @param orderId The order ID
   * @returns Response message
   */
  async verifyPayment(orderId: string): Promise<string> {
    // Check the payment status using the checkout session
    const session = await this.createOrGetCheckoutSession(orderId);
    return `Payment status: ${session.paymentStatus}`;
  }
}

export const stripeService = StripeService.getInstance();
