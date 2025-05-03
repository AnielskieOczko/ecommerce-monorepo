import { Route } from 'react-router-dom';
import { adminRoutes } from './adminRoutes';
import { CustomerRoutes } from './customerRoutes';
import HomePage from '../pages/HomePage';
import { ProductList as PublicProductList } from '../pages/customer/products';

// Export routes to be used in App.tsx
export { CustomerRoutes, adminRoutes };
