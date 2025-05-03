import api from './api';
import {
  ProductCreateDTO,
  ProductResponseDTO,
  ProductUpdateDTO,
  ProductSearchCriteria,
} from '../types/product';
import { BaseFilters, PaginatedResponse } from '../types/common';

interface ProductFilters extends BaseFilters {
  categoryId?: number;
  minPrice?: number;
  maxPrice?: number;
}

const BASE_URL = '/api/v1/admin/products';
const PUBLIC_BASE_URL = '/api/v1/public/products';

// Updated product methods with pagination and filters
export const getAllProducts = async (
  params: ProductSearchCriteria
): Promise<PaginatedResponse<ProductResponseDTO>> => {
  try {
    const response = await api.get<PaginatedResponse<ProductResponseDTO>>(BASE_URL, {
      params: {
        search: params.search || undefined,
        categoryId: params.categoryId || undefined,
        minPrice: params.minPrice || undefined,
        maxPrice: params.maxPrice || undefined,
        minStockQuantity: params.minStockQuantity || undefined,
        maxStockQuantity: params.maxStockQuantity || undefined,
        page: params.page - 1, // Convert to 0-based for backend
        size: params.size,
        sort: params.sort, // Already in format "field:direction"
      },
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const getProductById = async (id: number): Promise<ProductResponseDTO> => {
  try {
    const response = await api.get<ProductResponseDTO>(`${BASE_URL}/${id}`);
    return response.data;
  } catch (error) {
    throw error;
  }
};

// Create product with form data for image upload
export const createProduct = async (product: ProductCreateDTO): Promise<ProductResponseDTO> => {
  try {
    // First upload all images
    const imagePromises = product.imageList.map((image) => uploadImage(image as unknown as File));

    const uploadedImageUrls = await Promise.all(imagePromises);

    // Create product with uploaded image URLs
    const productData = {
      ...product,
      imageList: uploadedImageUrls.map((url) => ({ url })),
    };

    const response = await api.post<ProductResponseDTO>(BASE_URL, productData);
    return response.data;
  } catch (error) {
    throw error;
  }
};

// Updated update product with partial updates and image handling
export const updateProduct = async (
  id: number,
  formData: FormData
): Promise<ProductResponseDTO> => {
  try {
    const response = await api.put<ProductResponseDTO>(`/api/v1/admin/products/${id}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const deleteProduct = async (id: number): Promise<void> => {
  try {
    await api.delete(`/api/v1/admin/products/${id}`);
  } catch (error) {
    throw error;
  }
};

// Add method to delete product image
export const deleteProductImage = async (productId: number, imageId: number): Promise<void> => {
  try {
    await api.delete(`/api/v1/admin/products/${productId}/images/${imageId}`);
  } catch (error) {
    throw error;
  }
};

// search and filter methods
export const searchProducts = async (query: string): Promise<ProductResponseDTO[]> => {
  try {
    const response = await api.get<ProductResponseDTO[]>(`${BASE_URL}/search`, {
      params: {
        query,
      },
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const getProductsByCategory = async (categoryId: number): Promise<ProductResponseDTO[]> => {
  try {
    const response = await api.get<ProductResponseDTO[]>(`${BASE_URL}/category/${categoryId}`);
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const uploadImage = async (file: File): Promise<string> => {
  try {
    const formData = new FormData();
    formData.append('image', file);

    const response = await api.post<{ url: string }>('/api/v1/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    return response.data.url;
  } catch (error) {
    throw error;
  }
};

// Public methods (no auth required)
export const getPublicProducts = async (
  params: ProductSearchCriteria
): Promise<PaginatedResponse<ProductResponseDTO>> => {
  try {
    const response = await api.get<PaginatedResponse<ProductResponseDTO>>(PUBLIC_BASE_URL, {
      params: {
        search: params.search || undefined,
        categoryId: params.categoryId || undefined,
        minPrice: params.minPrice || undefined,
        maxPrice: params.maxPrice || undefined,
        page: params.page - 1, // Convert to 0-based for backend
        size: params.size,
        sort: params.sort, // Already in format "field:direction"
      },
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

// Export all functions as a single object
export const productService = {
  getAllProducts,
  getProductById,
  createProduct,
  updateProduct,
  deleteProduct,
  deleteProductImage,
  searchProducts,
  getProductsByCategory,
  uploadImage,
  getPublicProducts,
};

export default productService;
