export interface ErrorResponse {
  status: number;
  message: string;
  timestamp: string;
}

export interface ImageDTO {
  id: number;
  path: string;
  altText: string;
  mimeType: string;
}

export interface AddressDTO {
  street: string;
  city: string;
  zipCode: string;
  country: string;
}

export interface PhoneNumberDTO {
  value: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // current page
  first: boolean;
  last: boolean;
}

export type SortDirection = 'asc' | 'desc';

export interface SortField {
  field: string;
  direction: SortDirection;
}

export const formatSort = (field: string, direction: SortDirection): string => {
  return `${field}:${direction}`;
};

export interface PageRequest {
  page: number;
  size: number;
  sort: string;
  search?: string;
}

export interface BaseFilters extends PageRequest {
  search?: string;
}
