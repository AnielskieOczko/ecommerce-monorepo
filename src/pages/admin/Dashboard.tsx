import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Box, Typography, Container, Paper } from '@mui/material';

interface DashboardStats {
  totalOrders: number;
  totalRevenue: number;
  totalProducts: number;
  totalUsers: number;
  recentOrders: Array<{
    id: number;
    date: string;
    status: string;
    total: number;
  }>;
  topProducts: Array<{
    id: number;
    name: string;
    sales: number;
  }>;
}

const Dashboard = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDashboardStats = async () => {
      try {
        // Replace with your actual API call
        const response = await fetch('/api/admin/dashboard-stats');
        const data = await response.json();
        setStats(data);
      } catch (err) {
        setError('Failed to fetch dashboard statistics');
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardStats();
  }, []);

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;
  if (!stats) return <div>No data available</div>;

  return (
    <Container maxWidth="lg">
      <Paper elevation={3} sx={{ p: 3, mt: 3 }}>
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            mb: 4,
            borderBottom: '1px solid #e0e0e0',
            pb: 2,
          }}
        >
          <Typography variant="h4" component="h1">
            Admin Dashboard
          </Typography>
        </Box>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <StatsCard
            title="Total Orders"
            value={stats.totalOrders}
            link="/admin/orders"
            trend="+12.5%"
            icon="📦"
          />
          <StatsCard
            title="Total Revenue"
            value={`$${stats.totalRevenue.toLocaleString()}`}
            link="/admin/orders"
            trend="+8.2%"
            icon="💰"
          />
          <StatsCard
            title="Total Products"
            value={stats.totalProducts}
            link="/admin/products"
            trend="+5.1%"
            icon="🏷️"
          />
          <StatsCard
            title="Total Users"
            value={stats.totalUsers}
            link="/admin/users"
            trend="+15.3%"
            icon="👥"
          />
        </div>

        {/* Recent Orders & Top Products */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Recent Orders */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-lg font-semibold">Recent Orders</h2>
              <Link to="/admin/orders" className="text-blue-500 hover:text-blue-600">
                View All
              </Link>
            </div>
            <div className="space-y-4">
              {stats.recentOrders.map((order) => (
                <div
                  key={order.id}
                  className="flex justify-between items-center p-4 bg-gray-50 rounded"
                >
                  <div>
                    <div className="font-medium">Order #{order.id}</div>
                    <div className="text-sm text-gray-500">{order.date}</div>
                  </div>
                  <div className="flex items-center gap-4">
                    <span
                      className={`px-2 py-1 rounded text-sm ${
                        order.status === 'completed'
                          ? 'bg-green-100 text-green-800'
                          : order.status === 'pending'
                            ? 'bg-yellow-100 text-yellow-800'
                            : 'bg-gray-100 text-gray-800'
                      }`}
                    >
                      {order.status}
                    </span>
                    <span className="font-medium">${order.total}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Top Products */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-lg font-semibold">Top Products</h2>
              <Link to="/admin/products" className="text-blue-500 hover:text-blue-600">
                View All
              </Link>
            </div>
            <div className="space-y-4">
              {stats.topProducts.map((product) => (
                <div
                  key={product.id}
                  className="flex justify-between items-center p-4 bg-gray-50 rounded"
                >
                  <div className="font-medium">{product.name}</div>
                  <div className="text-sm">{product.sales} sales</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </Paper>
    </Container>
  );
};

// Stats Card Component
interface StatsCardProps {
  title: string;
  value: string | number;
  link: string;
  trend: string;
  icon: string;
}

const StatsCard: React.FC<StatsCardProps> = ({ title, value, link, trend, icon }) => (
  <Link to={link} className="bg-white rounded-lg shadow p-6 hover:shadow-md transition-shadow">
    <div className="flex justify-between items-start">
      <div>
        <div className="text-gray-500 text-sm">{title}</div>
        <div className="text-2xl font-semibold mt-1">{value}</div>
        <div className="text-green-500 text-sm mt-2">{trend}</div>
      </div>
      <div className="text-2xl">{icon}</div>
    </div>
  </Link>
);

export default Dashboard;
