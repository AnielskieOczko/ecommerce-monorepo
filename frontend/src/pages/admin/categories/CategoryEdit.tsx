import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { categoryService } from '../../../services/categoryService';

const CategoryEdit = () => {
  const { categoryId } = useParams();
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  console.log('Route params - categoryId:', categoryId);

  useEffect(() => {
    const fetchCategory = async () => {
      try {
        if (!categoryId) {
          console.log('No category ID provided');
          setError('No category ID provided');
          setLoading(false);
          return;
        }

        const parsedId = parseInt(categoryId);
        console.log('Parsed category ID:', parsedId);

        if (isNaN(parsedId)) {
          console.log('Failed to parse ID:', categoryId);
          setError(`Invalid category ID format: "${categoryId}"`);
          setLoading(false);
          return;
        }

        console.log('Fetching category with ID:', parsedId);
        const category = await categoryService.getCategoryById(parsedId);
        console.log('Fetched category:', category);
        setName(category.name);
      } catch (err) {
        console.error('Error fetching category:', err);
        setError(err instanceof Error ? err.message : 'Failed to fetch category');
      } finally {
        setLoading(false);
      }
    };

    fetchCategory();
  }, [categoryId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (!categoryId) {
        setError('No category ID provided');
        return;
      }

      const parsedId = parseInt(categoryId);
      if (isNaN(parsedId)) {
        setError(`Invalid category ID format: "${categoryId}"`);
        return;
      }

      await categoryService.updateCategory(parsedId, { name });
      navigate('/admin/categories');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update category');
    }
  };

  if (loading) return <div>Loading...</div>;
  if (error)
    return (
      <div className="p-4 bg-red-50 text-red-600 rounded-lg">
        <h2 className="text-lg font-semibold mb-2">Error</h2>
        <p>{error}</p>
        <button
          onClick={() => navigate('/admin/categories')}
          className="mt-4 px-4 py-2 bg-red-100 text-red-700 rounded hover:bg-red-200"
        >
          Back to Categories
        </button>
      </div>
    );

  return (
    <div>
      <h1 className="text-2xl font-semibold mb-6">Edit Category</h1>

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
            Update Category
          </button>
        </div>
      </form>
    </div>
  );
};

export default CategoryEdit;
