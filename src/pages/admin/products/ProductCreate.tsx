import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { productService } from '../../../services/productService';
import { categoryService } from '../../../services/categoryService';
import ImageUpload from '../../../components/admin/ImageUpload';
import { ProductCreateDTO } from '../../../types/product';
import { CategoryDTO } from '../../../types/category';
import api from '../../../services/api';

const ProductCreate = () => {
  const navigate = useNavigate();
  const [images, setImages] = useState<File[]>([]);
  const [categories, setCategories] = useState<CategoryDTO[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await categoryService.getAllCategories({
          page: 1,
          size: 100,
          sort: 'id:asc',
        });
        setCategories(response.content);
      } catch (err) {
        setError('Failed to load categories');
      }
    };
    fetchCategories();
  }, []);

  const handleImageSelect = (files: File[]) => {
    setImages(files);
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData();

    const productData = {
      name: (e.currentTarget.elements.namedItem('name') as HTMLInputElement).value,
      description: (e.currentTarget.elements.namedItem('description') as HTMLTextAreaElement).value,
      price: Number((e.currentTarget.elements.namedItem('price') as HTMLInputElement).value),
      currencyCode: (e.currentTarget.elements.namedItem('currencyCode') as HTMLSelectElement).value,
      quantity: Number((e.currentTarget.elements.namedItem('quantity') as HTMLInputElement).value),
      categoryIds: [
        Number((e.currentTarget.elements.namedItem('categoryId') as HTMLSelectElement).value),
      ],
      imageList: [],
    };

    formData.append(
      'product',
      new Blob([JSON.stringify(productData)], {
        type: 'application/json',
      })
    );

    // Add images
    images.forEach((file) => {
      formData.append('images', file);
    });

    try {
      const response = await api.post('/api/v1/admin/products', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
          Accept: 'application/json',
        },
      });
      navigate('/admin/products');
    } catch (error) {
      setError('Failed to create product');
    }
  };

  return (
    <div className="max-w-4xl mx-auto p-6">
      <h1 className="text-2xl font-semibold mb-6">Create New Product</h1>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Basic Information */}
        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-lg font-medium mb-4">Basic Information</h2>

          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Product Name</label>
              <input
                type="text"
                name="name"
                required
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">Description</label>
              <textarea
                name="description"
                rows={4}
                required
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Price</label>
                <input
                  type="number"
                  name="price"
                  step="0.01"
                  required
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">Currency</label>
                <select
                  name="currencyCode"
                  required
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                >
                  <option value="USD">USD</option>
                  <option value="EUR">EUR</option>
                  <option value="PLN">PLN</option>
                </select>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">Quantity</label>
              <input
                type="number"
                name="quantity"
                min="0"
                required
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">Category</label>
              <select
                name="categoryId"
                required
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
              >
                <option value="">Select a category</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </div>

        {/* Images */}
        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-lg font-medium mb-4">Product Images</h2>

          <ImageUpload onImageSelect={handleImageSelect} />

          {images.length > 0 && (
            <div className="mt-4 grid grid-cols-2 gap-4">
              {images.map((file, index) => (
                <div key={index} className="space-y-2">
                  <img
                    src={URL.createObjectURL(file)}
                    alt={`Preview ${index + 1}`}
                    className="w-full h-32 object-cover rounded"
                  />
                  <input
                    type="text"
                    name={`altText${index}`}
                    placeholder="Image description"
                    className="w-full px-3 py-2 border rounded-md"
                  />
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="flex justify-end">
          <button
            type="submit"
            className="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700"
          >
            Create Product
          </button>
        </div>
      </form>
    </div>
  );
};

export default ProductCreate;
