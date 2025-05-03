import api from './api';
import {
  UserResponseDTO,
  CreateUserRequest,
  AdminUpdateUserRequest,
  AccountStatusRequest,
  AdminChangeUserAuthorityRequest,
  AdminSearchUsersRequest,
} from '../types/user';
import { PaginatedResponse } from '../types/common';

const BASE_URL = '/api/v1/admin/users';

export const adminUserService = {
  getUserById: async (userId: number): Promise<UserResponseDTO> => {
    const response = await api.get<UserResponseDTO>(`${BASE_URL}/${userId}`);
    return response.data;
  },

  getAllUsers: async (
    params: AdminSearchUsersRequest
  ): Promise<PaginatedResponse<UserResponseDTO>> => {
    try {
      console.log('Fetching users with params:', params); // Debug log

      const response = await api.get<PaginatedResponse<UserResponseDTO>>(BASE_URL, {
        params: {
          search: params.search || undefined,
          isActive: params.isActive,
          authority: params.authority || undefined,
          page: params.page - 1, // Convert to 0-based for backend
          size: params.size,
          sort: params.sort, // Already in format "field:direction"
        },
      });

      console.log('Users response:', response.data); // Debug log
      return response.data;
    } catch (error) {
      console.error('Error fetching users:', error);
      throw error;
    }
  },

  createUser: async (request: CreateUserRequest): Promise<UserResponseDTO> => {
    const response = await api.post<UserResponseDTO>(BASE_URL, request);
    return response.data;
  },

  updateUser: async (userId: number, request: AdminUpdateUserRequest): Promise<UserResponseDTO> => {
    const response = await api.put<UserResponseDTO>(`${BASE_URL}/${userId}`, request);
    return response.data;
  },

  updateAccountStatus: async (
    userId: number,
    request: AccountStatusRequest
  ): Promise<UserResponseDTO> => {
    const response = await api.put<UserResponseDTO>(`${BASE_URL}/${userId}/status`, request);
    return response.data;
  },

  updateUserAuthorities: async (
    userId: number,
    request: AdminChangeUserAuthorityRequest
  ): Promise<UserResponseDTO> => {
    const response = await api.put<UserResponseDTO>(`${BASE_URL}/${userId}/authorities`, request);
    return response.data;
  },

  deleteUser: async (userId: number): Promise<void> => {
    await api.delete(`${BASE_URL}/${userId}`);
  },
};

export default adminUserService;
