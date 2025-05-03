import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { handleApiError } from '../middleware/errorMiddleware';

// Define custom metadata for requests
interface RequestMetadata {
  startTime: Date;
}

declare module 'axios' {
  export interface InternalAxiosRequestConfig {
    metadata?: RequestMetadata;
  }
}

const api = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000, // 10 seconds timeout
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request tracking for loading states
let activeRequests = 0;

const updateLoadingState = (isLoading: boolean) => {
  const loader = document.getElementById('global-loader');
  if (loader) {
    loader.style.display = isLoading ? 'block' : 'none';
  }
};

// Request interceptor
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Add request timing metadata
    config.metadata = { startTime: new Date() };

    // Add auth token
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Update loading state
    activeRequests++;
    updateLoadingState(true);

    return config;
  },
  (error: AxiosError) => {
    activeRequests--;
    updateLoadingState(activeRequests > 0);
    return Promise.reject(handleApiError(error));
  }
);

// Response interceptor
api.interceptors.response.use(
  (response) => {
    // Log request duration
    const endTime = new Date();
    const startTime = response.config.metadata?.startTime;
    if (startTime) {
      const duration = endTime.getTime() - startTime.getTime();
      console.debug(`Request to ${response.config.url} took ${duration}ms`);
    }

    // Update loading state
    activeRequests--;
    updateLoadingState(activeRequests > 0);

    return response;
  },
  async (error: AxiosError) => {
    // Update loading state
    activeRequests--;
    updateLoadingState(activeRequests > 0);

    // Handle token refresh
    if (error.response?.status === 401 && error.config) {
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) {
          throw new Error('No refresh token available');
        }

        // Attempt to refresh token
        const response = await axios.post(`${process.env.REACT_APP_API_URL}/auth/refresh`, {
          refreshToken,
        });

        const { token } = response.data;
        localStorage.setItem('token', token);

        // Retry original request
        const originalRequest = error.config;
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${token}`;
        }
        return axios(originalRequest);
      } catch (refreshError) {
        // Clear auth tokens and redirect to login
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
      }
    }

    return Promise.reject(handleApiError(error));
  }
);

export default api;
