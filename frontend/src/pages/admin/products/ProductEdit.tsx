import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ProductResponseDTO, ProductUpdateDTO } from '../../../types/product';
import { CategoryDTO } from '../../../types/category';
import { productService } from '../../../services/productService';
import ImageUpload from '../../../components/admin/ImageUpload';
import { categoryService } from '../../../services/categoryService';
import { getImageUrl } from '../../../utils/imageUtils';

const ProductEdit = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [categories, setCategories] = useState<CategoryDTO[]>([]);
  const [product, setProduct] = useState<ProductResponseDTO | null>(null);
  const [selectedCategories, setSelectedCategories] = useState<number[]>([]);
  const [images, setImages] = useState<File[]>([]);

  // Fetch product and categories data
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [productData, categoriesData] = await Promise.all([
          productService.getProductById(Number(id)),
          categoryService.getAllCategories({
            page: 1,
            size: 100,
            sort: 'id:asc',
          }),
        ]);

        setProduct(productData);
        setCategories(categoriesData.content);
        setSelectedCategories(productData.categories.map((cat) => cat.id));
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to fetch product');
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchData();
    }
  }, [id]);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!product) return;

    try {
      const form = e.currentTarget;
      const formData = new FormData();

      const nameInput = form.elements.namedItem('name') as HTMLInputElement;
      const descriptionInput = form.elements.namedItem('description') as HTMLTextAreaElement;
      const priceInput = form.elements.namedItem('price') as HTMLInputElement;
      const currencyInput = form.elements.namedItem('currencyCode') as HTMLSelectElement;
      const quantityInput = form.elements.namedItem('quantity') as HTMLInputElement;

      if (!nameInput || !descriptionInput || !priceInput || !currencyInput || !quantityInput) {
        throw new Error('Required form fields are missing');
      }

      // Add product data as JSON
      const updateData: ProductUpdateDTO = {
        name: nameInput.value,
        description: descriptionInput.value,
        price: Number(parseFloat(priceInput.value).toFixed(2)),
        currencyCode: currencyInput.value,
        quantity: Math.floor(Number(quantityInput.value)),
        categoryIds: selectedCategories,
        imageList: product.imageList,
      };

      formData.append(
        'product',
        new Blob([JSON.stringify(updateData)], { type: 'application/json' })
      );

      // Add new images if any
      images.forEach((image) => {
        formData.append('images', image);
      });

      await productService.updateProduct(Number(id), formData);
      navigate('/admin/products');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update product');
    }
  };

  const handleImageDelete = async (imageId: number | undefined) => {
    if (!product || !id || !imageId) return;

    try {
      await productService.deleteProductImage(Number(id), imageId);
      setProduct({
        ...product,
        imageList: product.imageList.filter((img) => img.id !== imageId),
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete image');
    }
  };

  // Add handler for image selection
  const handleImageSelect = (files: File[]) => {
    setImages(files);
  };

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;
  if (!product) return <div>Product not found</div>;

  return (
    <div className="max-w-4xl mx-auto py-6">
      <h1 className="text-2xl font-semibold mb-6">Edit Product</h1>

      <form onSubmit={handleSubmit} className="space-y-6 bg-white rounded-lg shadow p-6">
        {/* Basic Information */}
        <div className="grid grid-cols-2 gap-6">
          <div>
            <label className="block text-sm font-medium text-gray-700">Name</label>
            <input
              type="text"
              name="name"
              defaultValue={product.name}
              className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Price</label>
            <input
              type="number"
              name="price"
              defaultValue={product.price}
              step="0.01"
              className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Quantity</label>
            <input
              type="number"
              name="quantity"
              defaultValue={product.quantity}
              min="1"
              className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
              required
            />
          </div>
        </div>

        {/* Currency Code */}
        <div>
          <label className="block text-sm font-medium text-gray-700">Currency</label>
          <select
            name="currencyCode"
            defaultValue={product.currencyCode}
            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
            required
          >
            <option value="USD">USD</option>
            <option value="EUR">EUR</option>
            <option value="PLN">PLN</option>
          </select>
        </div>

        {/* Description */}
        <div>
          <label className="block text-sm font-medium text-gray-700">Description</label>
          <textarea
            name="description"
            defaultValue={product.description}
            rows={4}
            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
            required
          />
        </div>

        {/* Categories */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Categories</label>
          <div className="grid grid-cols-3 gap-4">
            {categories.map((category) => (
              <label key={category.id} className="flex items-center space-x-2">
                <input
                  type="checkbox"
                  checked={selectedCategories.includes(category.id)}
                  onChange={(e) => {
                    if (e.target.checked) {
                      setSelectedCategories([...selectedCategories, category.id]);
                    } else {
                      setSelectedCategories(selectedCategories.filter((id) => id !== category.id));
                    }
                  }}
                  className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                />
                <span>{category.name}</span>
              </label>
            ))}
          </div>
        </div>

        {/* Current Images */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Current Images</label>
          <div className="grid grid-cols-4 gap-4">
            {product.imageList.map((image) => (
              <div key={image.id} className="relative group">
                <img
                  src={getImageUrl(image.path)}
                  alt={image.altText}
                  className="w-full h-32 object-cover rounded"
                  onError={(e) => {
                    e.currentTarget.onerror = null;
                    e.currentTarget.src = 'https://placehold.co/100x100?text=No+Image';
                  }}
                />
                {image.id && (
                  <button
                    type="button"
                    onClick={() => handleImageDelete(image.id)}
                    className="absolute top-2 right-2 p-1 bg-red-500 text-white rounded-full opacity-0 group-hover:opacity-100 transition-opacity"
                  >
                    ×
                  </button>
                )}
              </div>
            ))}
          </div>
        </div>

        {/* Add New Images */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Add New Images</label>
          <ImageUpload onImageSelect={handleImageSelect} />
          {/* Preview selected images */}
          {images.length > 0 && (
            <div className="mt-4 grid grid-cols-4 gap-4">
              {images.map((file, index) => (
                <div key={index} className="relative">
                  <img
                    src={URL.createObjectURL(file)}
                    alt={`Preview ${index + 1}`}
                    className="w-full h-32 object-cover rounded"
                  />
                  <button
                    type="button"
                    onClick={() => setImages(images.filter((_, i) => i !== index))}
                    className="absolute top-2 right-2 p-1 bg-red-500 text-white rounded-full"
                  >
                    ×
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Form Actions */}
        <div className="flex justify-end space-x-4">
          <button
            type="button"
            onClick={() => navigate('/admin/products')}
            className="px-4 py-2 text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
          >
            Cancel
          </button>
          <button
            type="submit"
            className="px-4 py-2 text-white bg-blue-500 rounded-md hover:bg-blue-600"
          >
            Update Product
          </button>
        </div>
      </form>
    </div>
  );
};

export default ProductEdit;
