import React, { useEffect, useState, useCallback } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  TextField,
  MenuItem,
  Container,
  Pagination,
  FormControl,
  InputLabel,
  Select,
  SelectChangeEvent,
  Button,
  Rating,
  IconButton,
  Drawer,
  Divider,
  Snackbar,
  Alert,
} from '@mui/material';
import FilterListIcon from '@mui/icons-material/FilterList';
import CloseIcon from '@mui/icons-material/Close';
import { useNavigate } from 'react-router-dom';
import { getPublicProducts } from '../../../services/productService';
import { categoryService } from '../../../services/categoryService';
import { ProductResponseDTO, ProductSearchCriteria } from '../../../types/product';
import { CategoryDTO } from '../../../types/category';
import { PaginatedResponse } from '../../../types/common';
import { getImageUrl } from '../../../utils/imageUtils';
import { debounce } from 'lodash';
import { useAuth } from '../../../contexts/AuthContext';
import { useCartContext } from '../../../contexts/CartContext';

const sortOptions = [
  { value: 'productName:asc', label: 'Name (A-Z)' },
  { value: 'productName:desc', label: 'Name (Z-A)' },
  { value: 'productPrice:asc', label: 'Price (Low to High)' },
  { value: 'productPrice:desc', label: 'Price (High to Low)' },
  { value: 'stockQuantity:desc', label: 'Stock (High to Low)' },
  { value: 'stockQuantity:asc', label: 'Stock (Low to High)' },
  { value: 'id:asc', label: 'Oldest First' },
  { value: 'id:desc', label: 'Newest First' },
];

interface FilterPanelProps {
  searchTerm: string;
  searchCriteria: ProductSearchCriteria;
  categories: CategoryDTO[];
  onSearchChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
  onSortChange: (event: SelectChangeEvent) => void;
  onCategoryChange: (event: SelectChangeEvent) => void;
  onClose: () => void;
}

const FilterPanel: React.FC<FilterPanelProps> = ({
  searchTerm,
  searchCriteria,
  categories,
  onSearchChange,
  onSortChange,
  onCategoryChange,
  onClose,
}) => (
  <div className="p-6 w-80">
    <div className="flex justify-between items-center mb-4">
      <h2 className="text-xl font-semibold">Filters</h2>
      <button onClick={onClose} className="p-1 hover:bg-gray-100 rounded-full">
        <CloseIcon />
      </button>
    </div>
    <div className="border-b mb-4" />
    <div className="space-y-4">
      <FormControl fullWidth>
        <TextField
          label="Search Products"
          variant="outlined"
          value={searchTerm}
          onChange={onSearchChange}
          autoComplete="off"
        />
      </FormControl>
      <FormControl fullWidth>
        <InputLabel>Sort By</InputLabel>
        <Select value={searchCriteria.sort} onChange={onSortChange} label="Sort By">
          {sortOptions.map((option) => (
            <MenuItem key={option.value} value={option.value}>
              {option.label}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
      <FormControl fullWidth>
        <InputLabel>Category</InputLabel>
        <Select
          value={searchCriteria.categoryId || 'all'}
          onChange={onCategoryChange}
          label="Category"
        >
          <MenuItem value="all">All Categories</MenuItem>
          {categories.map((category) => (
            <MenuItem key={category.id} value={category.id.toString()}>
              {category.name}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
    </div>
  </div>
);

const ProductList: React.FC = () => {
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAuth();
  const { addToCart } = useCartContext();
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [products, setProducts] = useState<ProductResponseDTO[]>([]);
  const [categories, setCategories] = useState<CategoryDTO[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success' as 'success' | 'error',
  });
  const [searchCriteria, setSearchCriteria] = useState<ProductSearchCriteria>({
    page: 1,
    size: 12,
    sort: 'productName:asc',
    search: '',
  });

  const fetchCategories = async () => {
    try {
      console.log('Fetching categories...');
      const data = await categoryService.getAllPublicCategories();
      console.log('Categories response:', data);
      setCategories(data || []);
    } catch (error) {
      console.error('Error fetching categories:', error);
      setCategories([]);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchProducts = async () => {
    try {
      const response: PaginatedResponse<ProductResponseDTO> =
        await getPublicProducts(searchCriteria);
      setProducts(response.content);
      setTotalPages(response.totalPages);
    } catch (error) {
      console.error('Error fetching products:', error);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, [searchCriteria]);

  const debouncedSearch = useCallback(
    debounce((value: string) => {
      setSearchCriteria((prev) => ({ ...prev, search: value, page: 1 }));
    }, 500),
    []
  );

  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const value = event.target.value;
    setSearchTerm(value);
    debouncedSearch(value);
  };

  const handleSortChange = (event: SelectChangeEvent) => {
    setSearchCriteria((prev) => ({ ...prev, sort: event.target.value, page: 1 }));
  };

  const handlePageChange = (event: React.ChangeEvent<unknown>, page: number) => {
    setSearchCriteria((prev) => ({ ...prev, page }));
  };

  const handleCategoryChange = (event: SelectChangeEvent) => {
    const categoryId = event.target.value;
    setSearchCriteria((prev) => ({
      ...prev,
      categoryId: categoryId === 'all' ? undefined : categoryId,
      page: 1,
    }));
  };

  const handleAddToCart = async (productId: number) => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }

    try {
      await addToCart(user!.id, productId, 1);
      setSnackbar({
        open: true,
        message: 'Product added to cart successfully',
        severity: 'success',
      });
    } catch (error) {
      console.error('Error adding to cart:', error);
      setSnackbar({
        open: true,
        message: 'Failed to add product to cart',
        severity: 'error',
      });
    }
  };

  const handleCloseSnackbar = () => {
    setSnackbar((prev) => ({ ...prev, open: false }));
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-semibold">Products</h1>
        <button
          onClick={() => setIsFilterOpen(true)}
          className="flex items-center px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50"
        >
          <FilterListIcon className="mr-2" />
          Filter
        </button>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
        {products.map((product) => (
          <div key={product.id} className="bg-white rounded-lg shadow-md overflow-hidden">
            <div className="p-4 bg-gray-50 flex items-center justify-center h-72">
              {product.imageList && product.imageList[0] && (
                <img
                  className="max-w-full max-h-60 object-contain"
                  src={getImageUrl(product.imageList[0].path)}
                  alt={product.imageList[0].altText || product.name}
                  onError={(e: React.SyntheticEvent<HTMLImageElement>) => {
                    e.currentTarget.src = '/placeholder-image.png';
                  }}
                />
              )}
            </div>
            <div className="p-4 text-center">
              <h3 className="text-lg font-semibold mb-2">{product.name}</h3>
              <div className="flex justify-center items-center mb-2">
                {/* Replace with custom star rating using Tailwind */}
                <div className="flex text-yellow-400">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <svg key={star} className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                    </svg>
                  ))}
                </div>
              </div>
              <p className="text-xl font-bold text-primary mb-4">{product.price.toFixed(2)} zł</p>
              <button
                onClick={() => handleAddToCart(product.id)}
                className="w-full bg-black text-white py-2 px-4 rounded-md hover:bg-gray-800 transition-colors"
              >
                Add to Cart
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Pagination */}
      <div className="mt-8 flex justify-center">
        <nav className="flex items-center space-x-2">
          {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
            <button
              key={page}
              onClick={(e) => handlePageChange(e, page)}
              className={`px-3 py-1 rounded-md ${
                searchCriteria.page === page
                  ? 'bg-black text-white'
                  : 'bg-white text-gray-700 hover:bg-gray-50'
              }`}
            >
              {page}
            </button>
          ))}
        </nav>
      </div>

      <Drawer anchor="right" open={isFilterOpen} onClose={() => setIsFilterOpen(false)}>
        <FilterPanel
          searchTerm={searchTerm}
          searchCriteria={searchCriteria}
          categories={categories}
          onSearchChange={handleSearchChange}
          onSortChange={handleSortChange}
          onCategoryChange={handleCategoryChange}
          onClose={() => setIsFilterOpen(false)}
        />
      </Drawer>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
      >
        <Alert onClose={handleCloseSnackbar} severity={snackbar.severity}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </div>
  );
};

export default ProductList;
