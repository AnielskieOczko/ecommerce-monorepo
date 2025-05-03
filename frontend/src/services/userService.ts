import api from './api';
import {
  UserResponseDTO,
  UpdateBasicDetailsRequest,
  ChangePasswordRequest,
  ChangeEmailRequest,
  AccountStatusRequest,
} from '../types/user';
import { AuthResponse } from '../types/auth';

const BASE_URL = '/api/v1/users';

export const userService = {
  getProfile: async (userId: number): Promise<UserResponseDTO> => {
    const response = await api.get<UserResponseDTO>(`${BASE_URL}/${userId}/profile`);
    return response.data;
  },

  updateBasicDetails: async (
    userId: number,
    request: UpdateBasicDetailsRequest
  ): Promise<UserResponseDTO> => {
    const response = await api.put<UserResponseDTO>(`${BASE_URL}/${userId}/details`, request);
    return response.data;
  },

  changeEmail: async (userId: number, request: ChangeEmailRequest): Promise<AuthResponse> => {
    const response = await api.put<AuthResponse>(`${BASE_URL}/${userId}/email`, request);
    return response.data;
  },

  changePassword: async (userId: number, request: ChangePasswordRequest): Promise<void> => {
    await api.put(`${BASE_URL}/${userId}/password`, request);
  },

  updateAccountStatus: async (
    userId: number,
    request: AccountStatusRequest
  ): Promise<UserResponseDTO> => {
    const response = await api.put<UserResponseDTO>(`${BASE_URL}/${userId}/status`, request);
    return response.data;
  },

  deleteAccount: async (userId: number): Promise<void> => {
    await api.delete(`${BASE_URL}/${userId}`);
  },
};

export default userService;
