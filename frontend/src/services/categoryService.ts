import api from './api';
import {
  CategoryDTO,
  CreateCategoryDTO,
  UpdateCategoryDTO,
  CategorySearchCriteria,
} from '../types/category';
import { PaginatedResponse } from '../types/common';

const CATEGORY_URL_PUBLIC = '/api/v1/public/categories';
const CATEGORY_URL_ADMIN = '/api/v1/admin/categories';

export const categoryService = {
  // Public endpoints
  getAllPublicCategories: async (): Promise<CategoryDTO[]> => {
    const response = await api.get<PaginatedResponse<CategoryDTO>>(CATEGORY_URL_PUBLIC, {
      params: {
        page: 0,
        size: 100, // Get all categories in one request
        sort: 'name:asc',
      },
    });
    return response.data.content;
  },

  getCategoryNames: async (): Promise<string[]> => {
    const response = await api.get<string[]>(`${CATEGORY_URL_PUBLIC}/names`);
    return response.data;
  },

  // Admin endpoints
  getAllCategories: async (
    params: CategorySearchCriteria
  ): Promise<PaginatedResponse<CategoryDTO>> => {
    const response = await api.get<PaginatedResponse<CategoryDTO>>(CATEGORY_URL_ADMIN, {
      params: {
        page: params.page - 1, // Convert to 0-based for backend
        size: params.size,
        sort: params.sort,
        search: params.search || null,
        name: params.name || null,
      },
    });
    return response.data;
  },

  getCategoryById: async (id: number): Promise<CategoryDTO> => {
    try {
      const response = await api.get<CategoryDTO>(`${CATEGORY_URL_ADMIN}/${id}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  createCategory: async (category: CreateCategoryDTO): Promise<CategoryDTO> => {
    try {
      const response = await api.post<CategoryDTO>(CATEGORY_URL_ADMIN, category);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  updateCategory: async (id: number, category: UpdateCategoryDTO): Promise<CategoryDTO> => {
    try {
      const response = await api.put<CategoryDTO>(`${CATEGORY_URL_ADMIN}/${id}`, category);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  deleteCategory: async (id: number): Promise<void> => {
    try {
      await api.delete(`${CATEGORY_URL_ADMIN}/${id}`);
    } catch (error) {
      throw error;
    }
  },
};
