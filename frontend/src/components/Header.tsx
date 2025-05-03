import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useCartContext } from '../contexts/CartContext';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import { Badge, IconButton } from '@mui/material';

const Header: React.FC = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const { getItemCount } = useCartContext();
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  return (
    <header className="w-full">
      {/* Top bar */}
      <div className="bg-black text-white px-4 py-2 flex justify-between text-sm">
        <div className="flex space-x-4">
          <a href="#" className="hover:text-gray-300">
            Facebook
          </a>
          <a href="#" className="hover:text-gray-300">
            Instagram
          </a>
        </div>
        <div>Darmowa wysyłka od 299zł</div>
      </div>

      {/* Main navigation */}
      <nav className="container mx-auto px-4 py-4 flex items-center justify-between border-b">
        {/* Left menu */}
        <div className="hidden lg:flex space-x-6">
          <Link to="/about-me" className="hover:text-gray-600">
            About me
          </Link>
          <Link to="/products" className="hover:text-gray-600">
            Products
          </Link>
          <Link to="/blog" className="hover:text-gray-600">
            Blog
          </Link>
        </div>

        {/* Logo */}
        <Link to="/" className="text-2xl font-light">
          LOGO MOJEGO SKLEPU
        </Link>

        {/* Right menu */}
        <div className="flex items-center space-x-6">
          <div className="hidden lg:flex space-x-6">
            {isAuthenticated ? (
              <>
                {/* Greeting */}
                <span className="text-gray-600">Hello, {user?.email}</span>

                {/* User Menu Dropdown */}
                <div className="relative">
                  <button
                    onClick={() => setIsMenuOpen(!isMenuOpen)}
                    className="hover:text-gray-600"
                  >
                    My Account
                  </button>

                  {isMenuOpen && (
                    <div className="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-50">
                      <Link
                        to="/customer"
                        className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                      >
                        Dashboard
                      </Link>
                      <Link
                        to="/customer/orders"
                        className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                      >
                        My Orders
                      </Link>
                      <Link
                        to="/customer/settings"
                        className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                      >
                        Account Settings
                      </Link>
                      <button
                        onClick={logout}
                        className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                      >
                        Logout
                      </button>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <Link to="/login" className="hover:text-gray-600">
                Login
              </Link>
            )}
            <button className="hover:text-gray-600">Search</button>
          </div>
          <Link
            to={isAuthenticated ? '/customer/cart' : '/login'}
            className="flex items-center hover:text-gray-600"
          >
            <Badge badgeContent={getItemCount()} color="primary">
              <ShoppingCartIcon />
            </Badge>
          </Link>
        </div>
      </nav>
    </header>
  );
};

export default Header;
