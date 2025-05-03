import { Route } from 'react-router-dom';
import AdminLayout from '../pages/admin/AdminLayout';
import Dashboard from '../pages/admin/Dashboard';
import { CategoryList, CategoryCreate, CategoryEdit } from '../pages/admin/categories';
import { ProductList, ProductCreate, ProductEdit } from '../pages/admin/products';
import { UserList, UserCreate, UserEdit } from '../pages/admin/users';
import RouteGuard from '../components/guards/RouteGuard';

export const adminRoutes = (
  <Route
    path="/admin"
    element={
      <RouteGuard requiredRoles={['ROLE_ADMIN']}>
        <AdminLayout />
      </RouteGuard>
    }
  >
    <Route index element={<Dashboard />} />

    {/* Products */}
    <Route path="products">
      <Route index element={<ProductList />} />
      <Route path="create" element={<ProductCreate />} />
      <Route path=":id/edit" element={<ProductEdit />} />
    </Route>

    {/* Categories */}
    <Route path="categories">
      <Route index element={<CategoryList />} />
      <Route path="create" element={<CategoryCreate />} />
      <Route path=":id/edit" element={<CategoryEdit />} />
    </Route>

    {/* Users */}
    <Route path="users">
      <Route index element={<UserList />} />
      <Route path=":id/edit" element={<UserEdit />} />
    </Route>
  </Route>
);
