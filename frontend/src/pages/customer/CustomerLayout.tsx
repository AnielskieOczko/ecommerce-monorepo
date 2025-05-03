import { Outlet } from 'react-router-dom';
import CustomerSidebar from '../../components/customer/CustomerSidebar';

const CustomerLayout = () => {
  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto py-8">
        <div className="flex gap-8">
          <aside className="w-64 bg-white rounded-lg shadow">
            <CustomerSidebar />
          </aside>
          <main className="flex-1">
            <div className="bg-white rounded-lg shadow p-6">
              <Outlet />
            </div>
          </main>
        </div>
      </div>
    </div>
  );
};

export default CustomerLayout;
