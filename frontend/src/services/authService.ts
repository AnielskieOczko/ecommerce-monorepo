import api from './api';
import { LoginRequest, AuthResponse, TokenRefreshRequest } from '../types/auth';
import { CreateUserRequest } from '../types/user';

export const authService = {
  login: async (loginRequest: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>('/api/v1/auth/login', loginRequest);
    if (response.data.success && response.data.data.token) {
      localStorage.setItem('token', response.data.data.token);
      localStorage.setItem('refreshToken', response.data.data.refreshToken);
    }
    return response.data;
  },

  register: async (registerRequest: CreateUserRequest): Promise<void> => {
    await api.post('/api/v1/auth/register', registerRequest);
  },

  logout: async (): Promise<void> => {
    await api.post('/api/v1/auth/logout');
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
  },

  refreshToken: async (request: TokenRefreshRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>('/api/v1/auth/refresh-token', request);
    if (response.data.success && response.data.data.token) {
      localStorage.setItem('token', response.data.data.token);
      localStorage.setItem('refreshToken', response.data.data.refreshToken);
    }
    return response.data;
  },
};

export default authService;
