import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { CategoryDTO, CategorySearchCriteria, CategorySortField } from '../../../types/category';
import { PaginatedResponse, SortDirection } from '../../../types/common';
import { categoryService } from '../../../services/categoryService';
import debounce from 'lodash/debounce';

const CategoryList = () => {
  const [categories, setCategories] = useState<PaginatedResponse<CategoryDTO> | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [searchInput, setSearchInput] = useState('');
  const [categoryNames, setCategoryNames] = useState<string[]>([]);

  // Sort state
  const [sortField, setSortField] = useState<CategorySortField>('id');
  const [sortDirection, setSortDirection] = useState<SortDirection>('asc');

  const [filters, setFilters] = useState<CategorySearchCriteria>({
    page: 1,
    size: 10,
    sort: 'id:asc',
    search: undefined,
    name: undefined,
  });

  useEffect(() => {
    fetchCategories();
  }, [filters]);

  useEffect(() => {
    loadCategoryNames();
  }, []);

  const loadCategoryNames = async () => {
    try {
      const names = await categoryService.getCategoryNames();
      setCategoryNames(names);
    } catch (err) {
      console.error('Failed to load category names:', err);
    }
  };

  // Sort handler
  const handleSort = (field: CategorySortField) => {
    setSortField(field);
    setSortDirection((prev) => (field === sortField ? (prev === 'asc' ? 'desc' : 'asc') : 'asc'));
    setFilters((prev) => ({
      ...prev,
      sort: `${field}:${field === sortField ? (sortDirection === 'asc' ? 'desc' : 'asc') : 'asc'}`,
    }));
  };

  // Sort icon helper
  const getSortIcon = (field: CategorySortField) => {
    if (field !== sortField) return '↕️';
    return sortDirection === 'asc' ? '↑' : '↓';
  };

  const fetchCategories = async () => {
    try {
      setLoading(true);
      const data = await categoryService.getAllCategories(filters);
      console.log('Categories data:', data);
      setCategories(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch categories');
    } finally {
      setLoading(false);
    }
  };

  // Debounced search function
  const debouncedSearch = debounce((search: string) => {
    setFilters((prev) => ({
      ...prev,
      page: 1,
      search: search || undefined,
    }));
  }, 300);

  const handleNameFilterChange = (name: string) => {
    setFilters((prev) => ({
      ...prev,
      page: 1,
      name: name || undefined,
    }));
  };

  const handlePageChange = (page: number) => {
    setFilters((prev) => ({ ...prev, page }));
  };

  const handlePageSizeChange = (size: number) => {
    setFilters((prev) => ({ ...prev, page: 1, size }));
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this category?')) {
      try {
        await categoryService.deleteCategory(id);
        fetchCategories();
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to delete category');
      }
    }
  };

  if (loading && !categories) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;
  if (!categories) return null;

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-semibold">Categories</h1>
        <Link
          to="/admin/categories/create"
          className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
        >
          Add Category
        </Link>
      </div>

      {/* Filters */}
      <div className="mb-6 bg-white rounded-lg shadow p-4">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Search</label>
            <input
              type="text"
              value={searchInput}
              onChange={(e) => {
                setSearchInput(e.target.value);
                debouncedSearch(e.target.value);
              }}
              placeholder="Search categories..."
              className="w-full p-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Category Name</label>
            <select
              value={filters.name || ''}
              onChange={(e) => handleNameFilterChange(e.target.value)}
              className="w-full p-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">All Categories</option>
              {categoryNames.map((name) => (
                <option key={name} value={name}>
                  {name}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Items per page</label>
            <select
              value={filters.size}
              onChange={(e) => handlePageSizeChange(Number(e.target.value))}
              className="w-full p-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value={10}>10</option>
              <option value={25}>25</option>
              <option value={50}>50</option>
            </select>
          </div>
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-lg shadow">
        <table className="min-w-full">
          <thead>
            <tr className="border-b">
              <th
                className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-50"
                onClick={() => handleSort('id')}
              >
                ID {getSortIcon('id')}
              </th>
              <th
                className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-50"
                onClick={() => handleSort('name')}
              >
                Name {getSortIcon('name')}
              </th>
              <th className="px-6 py-3 text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {categories.content.map((category) => (
              <tr key={category.id} className="border-b hover:bg-gray-50">
                <td className="px-6 py-4">{category.id}</td>
                <td className="px-6 py-4">{category.name}</td>
                <td className="px-6 py-4 text-right">
                  <Link
                    to={`/admin/categories/${category.id}/edit`}
                    className="text-blue-500 hover:text-blue-700 mr-4"
                    onClick={(e) => {
                      console.log('Category:', category);
                      console.log('Edit URL:', `/admin/categories/${category.id}/edit`);
                    }}
                  >
                    Edit
                  </Link>
                  <button
                    onClick={() => handleDelete(category.id)}
                    className="text-red-500 hover:text-red-700"
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {/* Pagination */}
        {categories.totalPages > 1 && (
          <div className="flex justify-between items-center p-4 border-t">
            <div className="text-sm text-gray-500">
              Showing {(filters.page - 1) * filters.size + 1} to{' '}
              {Math.min(filters.page * filters.size, categories.totalElements)} of{' '}
              {categories.totalElements} entries
            </div>

            <div className="flex gap-2">
              <button
                onClick={() => handlePageChange(filters.page - 1)}
                disabled={filters.page === 1}
                className="px-3 py-1 rounded border disabled:opacity-50"
              >
                Previous
              </button>

              {Array.from({ length: categories.totalPages }, (_, i) => i + 1)
                .filter((page) => {
                  const current = filters.page;
                  return (
                    page === 1 ||
                    page === categories.totalPages ||
                    (page >= current - 1 && page <= current + 1)
                  );
                })
                .map((page) => (
                  <button
                    key={page}
                    onClick={() => handlePageChange(page)}
                    className={`px-3 py-1 rounded border ${
                      filters.page === page ? 'bg-blue-500 text-white' : 'hover:bg-gray-100'
                    }`}
                  >
                    {page}
                  </button>
                ))}

              <button
                onClick={() => handlePageChange(filters.page + 1)}
                disabled={filters.page === categories.totalPages}
                className="px-3 py-1 rounded border disabled:opacity-50"
              >
                Next
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default CategoryList;
