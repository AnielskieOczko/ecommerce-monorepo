import { NavLink } from 'react-router-dom';

const CustomerSidebar = () => {
  const navItems = [
    { to: '/customer/account', label: 'Account Overview', icon: '👤' },
    { to: '/customer/account/edit', label: 'Edit Profile', icon: '✏️' },
    { to: '/customer/account/security', label: 'Security', icon: '🔒' },
    { to: '/customer/orders', label: 'My Orders', icon: '📦' },
  ];

  return (
    <nav className="py-6 flex flex-col h-full">
      <div className="flex-1">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === '/customer/account'}
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
      </div>
      <div className="border-t border-gray-200 pt-4 mt-4">
        <NavLink to="/" className="flex items-center px-6 py-3 text-gray-700 hover:bg-gray-100">
          <span className="mr-3">🏠</span>
          Return to Home
        </NavLink>
      </div>
    </nav>
  );
};

export default CustomerSidebar;
