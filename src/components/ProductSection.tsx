import { useEffect, useState } from 'react';
import { productService } from '../services/productService';
import { ProductResponseDTO } from '../types/product';

interface ProductSectionProps {
  title: string;
  bgColor: string;
}

const ProductSection: React.FC<ProductSectionProps> = ({ title, bgColor }) => {
  const [products, setProducts] = useState<ProductResponseDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        setLoading(true);
        const data = await productService.getPublicProducts({
          page: 1,
          size: 10,
          sort: 'id:asc',
          search: '',
        });
        setProducts(data.content);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to fetch products');
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, []);

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <section className={`py-16 ${bgColor}`}>
      <div className="container mx-auto px-4">
        <h2 className="text-3xl font-light mb-8">{title}</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {products.map((product) => (
            <div key={product.id}>
              <h3>{product.name}</h3>
              <p>{product.description}</p>
              <p>Price: ${product.price}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default ProductSection;
