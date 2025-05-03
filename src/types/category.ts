import { BaseFilters } from './common';

export interface CategoryDTO {
  id: number;
  name: string;
  description?: string;
}

export interface CreateCategoryDTO {
  name: string;
  description?: string;
}

export interface UpdateCategoryDTO {
  name?: string;
  description?: string;
}

export interface CategorySearchCriteria extends BaseFilters {
  name?: string;
}

export type CategorySortField = 'id' | 'name';
