import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { CreateUserRequest } from '../../types/user';
import { authService } from '../../services/authService';

const Register = () => {
  const navigate = useNavigate();
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);

    if (formData.get('password') !== formData.get('confirmPassword')) {
      setError('Passwords do not match');
      return;
    }

    const registerRequest: CreateUserRequest = {
      email: formData.get('email') as string,
      password: formData.get('password') as string,
      firstName: formData.get('firstName') as string,
      lastName: formData.get('lastName') as string,
      address: {
        street: formData.get('street') as string,
        city: formData.get('city') as string,
        zipCode: formData.get('zipCode') as string,
        country: formData.get('country') as string,
      },
      phoneNumber: {
        value: formData.get('phone') as string,
      },
      dateOfBirth: formData.get('dateOfBirth') as string,
      authorities: ['ROLE_USER'],
    };

    try {
      await authService.register(registerRequest);
      navigate('/login');
    } catch (err) {
      setError('Registration failed');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full p-8 bg-white rounded-lg shadow-md">
        <h2 className="text-2xl font-medium text-center mb-6">Create Account</h2>

        <form onSubmit={handleSubmit} className="space-y-4">
          {error && <div className="text-red-500 text-center">{error}</div>}

          {/* Personal Information */}
          <div className="grid grid-cols-2 gap-4">
            <input
              type="text"
              name="firstName"
              placeholder="First Name"
              required
              className="w-full px-3 py-2 border rounded-md"
            />
            <input
              type="text"
              name="lastName"
              placeholder="Last Name"
              required
              className="w-full px-3 py-2 border rounded-md"
            />
          </div>

          <input
            type="email"
            name="email"
            placeholder="Email"
            required
            className="w-full px-3 py-2 border rounded-md"
          />

          <input
            type="password"
            name="password"
            placeholder="Password"
            required
            className="w-full px-3 py-2 border rounded-md"
          />

          <input
            type="password"
            name="confirmPassword"
            placeholder="Confirm Password"
            required
            className="w-full px-3 py-2 border rounded-md"
          />

          <input
            type="tel"
            name="phone"
            placeholder="Phone Number"
            required
            className="w-full px-3 py-2 border rounded-md"
          />

          <input
            type="date"
            name="dateOfBirth"
            required
            className="w-full px-3 py-2 border rounded-md"
          />

          {/* Address */}
          <div className="space-y-4">
            <input
              type="text"
              name="street"
              placeholder="Street Address"
              required
              className="w-full px-3 py-2 border rounded-md"
            />
            <div className="grid grid-cols-2 gap-4">
              <input
                type="text"
                name="city"
                placeholder="City"
                required
                className="w-full px-3 py-2 border rounded-md"
              />
              <input
                type="text"
                name="zipCode"
                placeholder="ZIP Code"
                required
                className="w-full px-3 py-2 border rounded-md"
              />
            </div>
            <input
              type="text"
              name="country"
              placeholder="Country"
              required
              className="w-full px-3 py-2 border rounded-md"
            />
          </div>

          <button
            type="submit"
            className="w-full py-3 px-4 bg-gray-900 text-white rounded-md hover:bg-gray-800"
          >
            Create Account
          </button>
        </form>

        <div className="mt-6 text-center">
          <Link to="/login" className="text-sm text-blue-600 hover:text-blue-500">
            Already have an account? Sign in
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Register;
