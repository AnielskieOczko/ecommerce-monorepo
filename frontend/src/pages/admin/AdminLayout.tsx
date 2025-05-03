import { Outlet } from 'react-router-dom';
import AdminSidebar from '../../components/admin/AdminSidebar';
import AdminGuard from '../../components/admin/AdminGuard';

const AdminLayout = () => {
  return (
    // <AdminGuard>
      <div className="flex h-screen bg-gray-100">
        <aside className="w-64 bg-white shadow-md">
          <AdminSidebar />
        </aside>
        <main className="flex-1 overflow-y-auto p-8">
          <Outlet />
        </main>
      </div>
    // </AdminGuard>
  );
};

export default AdminLayout;
