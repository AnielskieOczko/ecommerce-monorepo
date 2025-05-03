import { NavLink, useNavigate } from 'react-router-dom';
import { useContext } from 'react';
import { AuthContext } from '../../contexts/AuthContext';
import { Button } from '@mui/material';
import LogoutIcon from '@mui/icons-material/Logout';

const AdminSidebar = () => {
  const navigate = useNavigate();
  const auth = useContext(AuthContext);

  const navItems = [
    { to: '/admin', label: 'Dashboard', icon: '📊' },
    { to: '/admin/products', label: 'Products', icon: '📦' },
    { to: '/admin/categories', label: 'Categories', icon: '🏷️' },
    { to: '/admin/users', label: 'Users', icon: '👥' },
    { to: '/admin/orders', label: 'Orders', icon: '🛍️' },
  ];

  const handleLogout = async () => {
    if (auth) {
      await auth.logout();
      navigate('/login');
    }
  };

  return (
    <div className="flex flex-col h-full">
      <nav className="py-6 flex-grow">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === '/admin'}
            className={({ isActive }) =>
              `flex items-center px-6 py-3 text-gray-700 hover:bg-gray-100 ${
                isActive ? 'bg-gray-100 font-medium' : ''
              }`
            }
          >
            <span className="mr-3">{item.icon}</span>
            {item.label}
          </NavLink>
        ))}
      </nav>
      <div className="p-4 border-t">
        <Button
          fullWidth
          variant="contained"
          color="error"
          onClick={handleLogout}
          startIcon={<LogoutIcon />}
          sx={{
            py: 1,
            fontWeight: 'medium',
          }}
        >
          Logout
        </Button>
      </div>
    </div>
  );
};

export default AdminSidebar;
