import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { productService } from '../../../services/productService';
import { categoryService } from '../../../services/categoryService';
import {
  ProductResponseDTO,
  ProductSortField,
  ProductSearchCriteria,
} from '../../../types/product';
import { CategoryDTO } from '../../../types/category';
import { PaginatedResponse, SortDirection } from '../../../types/common';
import { debounce } from 'lodash';
import { getImageUrl } from '../../../utils/imageUtils';

export const ProductList: React.FC = () => {
  const [products, setProducts] = useState<PaginatedResponse<ProductResponseDTO> | null>(null);
  const [categories, setCategories] = useState<CategoryDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [searchInput, setSearchInput] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [minPrice, setMinPrice] = useState<string>('');
  const [maxPrice, setMaxPrice] = useState<string>('');
  const [minStock, setMinStock] = useState<string>('');
  const [maxStock, setMaxStock] = useState<string>('');
  const [sortField, setSortField] = useState<ProductSortField>('productName');
  const [sortDirection, setSortDirection] = useState<SortDirection>('asc');
  const [filters, setFilters] = useState<ProductSearchCriteria>({
    page: 1,
    size: 10,
    sort: 'productName:asc',
    search: undefined,
    categoryId: undefined,
    minPrice: undefined,
    maxPrice: undefined,
    minStockQuantity: undefined,
    maxStockQuantity: undefined,
  });

  useEffect(() => {
    fetchProducts();
    fetchCategories();
  }, [filters]);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await productService.getAllProducts(filters);
      setProducts(data);
    } catch (err) {
      setError('Failed to fetch products');
      console.error('Error fetching products:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const response = await categoryService.getAllCategories({
        page: 1,
        size: 100,
        sort: 'id:asc',
      });
      setCategories(response.content);
    } catch (err) {
      console.error('Error fetching categories:', err);
    }
  };

  const handleSearchChange = debounce((value: string) => {
    setFilters((prev) => ({
      ...prev,
      page: 1,
      search: value || undefined,
    }));
  }, 300);

  const handleCategoryChange = (categoryId: string) => {
    setSelectedCategory(categoryId);
    setFilters((prev) => ({
      ...prev,
      page: 1,
      categoryId: categoryId || undefined,
    }));
  };

  const handleSort = (field: ProductSortField) => {
    const newDirection = field === sortField && sortDirection === 'asc' ? 'desc' : 'asc';
    setSortField(field);
    setSortDirection(newDirection);
    setFilters((prev) => ({
      ...prev,
      sort: `${field}:${newDirection}`,
    }));
  };

  const handlePageChange = (newPage: number) => {
    setFilters((prev) => ({
      ...prev,
      page: newPage,
    }));
  };

  const handlePageSizeChange = (newSize: number) => {
    setFilters((prev) => ({
      ...prev,
      page: 1,
      size: newSize,
    }));
  };

  const handlePriceChange = debounce((min: string, max: string) => {
    setFilters((prev) => ({
      ...prev,
      page: 1,
      minPrice: min ? Number(min) : undefined,
      maxPrice: max ? Number(max) : undefined,
    }));
  }, 300);

  const handleStockChange = debounce((min: string, max: string) => {
    setFilters((prev) => ({
      ...prev,
      page: 1,
      minStockQuantity: min ? Number(min) : undefined,
      maxStockQuantity: max ? Number(max) : undefined,
    }));
  }, 300);

  const getSortIcon = (field: ProductSortField) => {
    if (field !== sortField) return '↕️';
    return sortDirection === 'asc' ? '↑' : '↓';
  };

  if (loading) return <div>Loading...</div>;
  if (error) return <div className="text-red-500">{error}</div>;
  if (!products) return null;

  return (
    <div className="container mx-auto p-4">
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-2xl font-bold">Products</h1>
        <Link
          to="/admin/products/create"
          className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
        >
          Create Product
        </Link>
      </div>

      <div className="mb-4 space-y-4">
        <div className="flex gap-4">
          <input
            type="text"
            placeholder="Search products..."
            value={searchInput}
            onChange={(e) => {
              setSearchInput(e.target.value);
              handleSearchChange(e.target.value);
            }}
            className="border p-2 rounded"
          />

          <select
            value={selectedCategory}
            onChange={(e) => handleCategoryChange(e.target.value)}
            className="border p-2 rounded"
          >
            <option value="">All Categories</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </div>

        <div className="flex gap-4">
          <div className="flex gap-2 items-center">
            <span className="text-sm text-gray-600">Price Range:</span>
            <input
              type="number"
              placeholder="Min Price"
              value={minPrice}
              onChange={(e) => {
                setMinPrice(e.target.value);
                handlePriceChange(e.target.value, maxPrice);
              }}
              className="border p-2 rounded w-32"
              min="0"
              step="0.01"
            />
            <span>-</span>
            <input
              type="number"
              placeholder="Max Price"
              value={maxPrice}
              onChange={(e) => {
                setMaxPrice(e.target.value);
                handlePriceChange(minPrice, e.target.value);
              }}
              className="border p-2 rounded w-32"
              min="0"
              step="0.01"
            />
          </div>

          <div className="flex gap-2 items-center">
            <span className="text-sm text-gray-600">Stock Range:</span>
            <input
              type="number"
              placeholder="Min Stock"
              value={minStock}
              onChange={(e) => {
                setMinStock(e.target.value);
                handleStockChange(e.target.value, maxStock);
              }}
              className="border p-2 rounded w-32"
              min="0"
              step="1"
            />
            <span>-</span>
            <input
              type="number"
              placeholder="Max Stock"
              value={maxStock}
              onChange={(e) => {
                setMaxStock(e.target.value);
                handleStockChange(minStock, e.target.value);
              }}
              className="border p-2 rounded w-32"
              min="0"
              step="1"
            />
          </div>
        </div>
      </div>

      <div className="overflow-x-auto">
        <table className="min-w-full bg-white border">
          <thead>
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Image
              </th>
              <th
                className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                onClick={() => handleSort('id')}
              >
                ID {getSortIcon('id')}
              </th>
              <th
                className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                onClick={() => handleSort('productName')}
              >
                Name {getSortIcon('productName')}
              </th>
              <th
                className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                onClick={() => handleSort('productPrice')}
              >
                Price {getSortIcon('productPrice')}
              </th>
              <th
                className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                onClick={() => handleSort('stockQuantity')}
              >
                Quantity {getSortIcon('stockQuantity')}
              </th>
              <th
                className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                onClick={() => handleSort('categories')}
              >
                Category {getSortIcon('categories')}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody>
            {products.content.map((product) => (
              <tr key={product.id} className="border-t hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap">
                  {product.imageList && product.imageList[0] ? (
                    <img
                      src={getImageUrl(product.imageList[0].path)}
                      alt={product.imageList[0].altText || product.name}
                      className="h-16 w-16 object-cover rounded-md"
                      onError={(e) => {
                        e.currentTarget.onerror = null;
                        e.currentTarget.src = 'https://placehold.co/100x100?text=No+Image';
                      }}
                    />
                  ) : (
                    <div className="h-16 w-16 bg-gray-200 rounded-md flex items-center justify-center">
                      <span className="text-gray-400">No image</span>
                    </div>
                  )}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">{product.id}</td>
                <td className="px-6 py-4 whitespace-nowrap">{product.name}</td>
                <td className="px-6 py-4 whitespace-nowrap">${product.price.toFixed(2)}</td>
                <td className="px-6 py-4 whitespace-nowrap">{product.quantity}</td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {product.categories[0]?.name || 'No category'}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <Link
                    to={`/admin/products/${product.id}/edit`}
                    className="text-blue-600 hover:text-blue-900 mr-4"
                  >
                    Edit
                  </Link>
                  <button
                    onClick={() => {
                      /* TODO: Implement delete functionality */
                    }}
                    className="text-red-600 hover:text-red-900"
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="mt-4 flex justify-between items-center">
        <div>
          <select
            value={filters.size}
            onChange={(e) => handlePageSizeChange(Number(e.target.value))}
            className="border p-2 rounded"
          >
            <option value="10">10 per page</option>
            <option value="20">20 per page</option>
            <option value="50">50 per page</option>
          </select>
        </div>

        <div className="flex gap-2">
          <button
            onClick={() => handlePageChange(filters.page - 1)}
            disabled={filters.page === 1}
            className="px-4 py-2 border rounded disabled:opacity-50"
          >
            Previous
          </button>
          <span className="px-4 py-2">
            Page {filters.page} of {Math.ceil(products.totalElements / filters.size)}
          </span>
          <button
            onClick={() => handlePageChange(filters.page + 1)}
            disabled={filters.page >= Math.ceil(products.totalElements / filters.size)}
            className="px-4 py-2 border rounded disabled:opacity-50"
          >
            Next
          </button>
        </div>
      </div>
    </div>
  );
};

export default ProductList;
