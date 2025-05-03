import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { productService } from '../../../services/productService';
import { categoryService } from '../../../services/categoryService';

const CategoryCreate = () => {
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await categoryService.createCategory({ name });
      navigate('/admin/categories');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create category');
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-semibold mb-6">Create Category</h1>

      {error && <div className="bg-red-50 text-red-500 p-4 rounded mb-4">{error}</div>}

      <form onSubmit={handleSubmit} className="max-w-md bg-white rounded-lg shadow p-6">
        <div className="mb-4">
          <label htmlFor="name" className="block text-gray-700 mb-2">
            Category Name
          </label>
          <input
            type="text"
            id="name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full p-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            required
          />
        </div>

        <div className="flex justify-end gap-4">
          <button
            type="button"
            onClick={() => navigate('/admin/categories')}
            className="px-4 py-2 text-gray-600 hover:text-gray-800"
          >
            Cancel
          </button>
          <button
            type="submit"
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
          >
            Create Category
          </button>
        </div>
      </form>
    </div>
  );
};

export default CategoryCreate;
