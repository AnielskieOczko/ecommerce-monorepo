export type ErrorType = 'API' | 'NETWORK' | 'AUTH' | 'VALIDATION' | 'PAYMENT';

export interface AppError {
  type: ErrorType;
  status?: number;
  message: string;
  data?: unknown;
}

// Error factory functions
export const createApiError = (status: number, message: string, data?: unknown): AppError => ({
  type: 'API',
  status,
  message,
  data
});

export const createNetworkError = (message = 'Network error occurred'): AppError => ({
  type: 'NETWORK',
  message
});

export const createAuthError = (message = 'Authentication failed'): AppError => ({
  type: 'AUTH',
  status: 401,
  message
});

export const createPaymentError = (message: string, data?: unknown): AppError => ({
  type: 'PAYMENT',
  status: 400,
  message,
  data
});

export const errors = {
  api: createApiError,
  network: createNetworkError,
  auth: createAuthError,
  payment: createPaymentError
};
