import { useAuth } from '../../hooks/useAuth';

const Dashboard = () => {
  const { user } = useAuth();

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-2xl font-semibold mb-6">Dashboard</h1>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {/* Recent Orders */}
        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-lg font-medium mb-4">Recent Orders</h2>
          {/* Add recent orders list */}
        </div>

        {/* Account Overview */}
        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-lg font-medium mb-4">Account Overview</h2>
          <div className="space-y-2">
            <p>Email: {user?.email}</p>
            {/* Add other user details */}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
