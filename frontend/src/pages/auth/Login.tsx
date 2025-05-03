import { useState, FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { LoginRequest, AuthResponse } from '../../types/auth';
import { authService } from '../../services/authService';

const Login = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [error, setError] = useState('');

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);

    const loginRequest: LoginRequest = {
      email: formData.get('email') as string,
      password: formData.get('password') as string,
    };

    try {
      const response = await authService.login(loginRequest);
      login(response.data);

      // Check if user is admin and redirect accordingly
      const isAdmin = response.data.roles.includes('ROLE_ADMIN');
      navigate(isAdmin ? '/admin' : '/');
    } catch (err) {
      setError('Invalid credentials');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full p-8 bg-white">
        {/* Logo */}
        <div className="text-center mb-8">
          <img src="/path-to-your-logo.png" alt="Logo" className="mx-auto h-12 w-auto" />
        </div>

        {/* Title */}
        <h2 className="text-2xl font-medium text-center mb-6">Zaloguj się</h2>

        {/* Description */}
        <p className="text-gray-600 text-center mb-8">Please provide your email and password.</p>

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-6">
          {error && <div className="text-red-500 text-center text-sm">{error}</div>}

          <div>
            <input
              type="email"
              name="email"
              placeholder="E-mail"
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
          </div>

          <div>
            <input
              type="password"
              name="password"
              placeholder="Password"
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
          </div>

          <button
            type="submit"
            className="w-full py-3 px-4 bg-gray-900 text-white rounded-md hover:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-900"
          >
            KONTYNUUJ
          </button>
        </form>

        {/* Links */}
        <div className="mt-6 text-center">
          <Link to="/register" className="text-sm text-blue-600 hover:text-blue-500">
            Don't have an account? Sign up
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Login;
