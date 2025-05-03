import { CategoryDTO } from './category';
import { BaseFilters } from './common';

// Value Objects
interface Amount {
  value: number; // BigDecimal in Java
}

interface CurrencyCode {
  code: string;
}

interface ProductPrice {
  amount: Amount;
  currency: CurrencyCode;
}

// DTOs
export interface ProductCreateDTO {
  name: string;
  description: string;
  price: number; // This will be converted to BigDecimal
  currencyCode: string; // This will be wrapped in CurrencyCode
  quantity: number; // This will be converted to Integer
  categoryIds: number[];
  imageList: ImageDTO[];
}

export interface ProductResponseDTO {
  id: number; // Long in Java
  name: string;
  description: string;
  price: number; // BigDecimal in Java
  currencyCode: string; // Added this field
  quantity: number; // Integer in Java
  categories: CategoryResponseDTO[];
  imageList: ImageDTO[];
}

export interface ProductUpdateDTO {
  name: string;
  description: string;
  price: number; // BigDecimal in Java
  currencyCode: string; // Will be wrapped in CurrencyCode
  quantity: number; // Integer in Java
  categoryIds: number[]; // List<Long> in Java
  imageList: ImageDTO[];
}

export interface ImageDTO {
  id?: number;
  path: string;
  altText?: string;
}

export interface CategoryResponseDTO {
  id: number;
  name: string;
  // other category fields...
}

export interface ProductSearchCriteria extends BaseFilters {
  categoryId?: string;
  minPrice?: number;
  maxPrice?: number;
  minStockQuantity?: number;
  maxStockQuantity?: number;
}

export type ProductSortField =
  | 'id'
  | 'categories'
  | 'stockQuantity'
  | 'productPrice'
  | 'productName';
