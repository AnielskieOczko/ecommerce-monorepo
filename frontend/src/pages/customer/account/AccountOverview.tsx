import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../../hooks/useAuth';
import { userService } from '../../../services/userService';
import { UserResponseDTO } from '../../../types/user';
import { toast } from 'react-toastify';

const AccountOverview = () => {
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);
  const [userData, setUserData] = useState<UserResponseDTO | null>(null);

  useEffect(() => {
    if (user?.id) {
      loadUserData();
    }
  }, [user]);

  const loadUserData = async () => {
    try {
      setLoading(true);
      const data = await userService.getProfile(user!.id);
      setUserData(data);
    } catch (error) {
      toast.error('Failed to load account information');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <div className="max-w-4xl mx-auto">
      <div className="bg-white shadow-md rounded-lg overflow-hidden">
        <div className="p-6">
          <h2 className="text-2xl font-semibold mb-6">Account Overview</h2>

          {/* Personal Information */}
          <div className="mb-8">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-medium">Personal Information</h3>
              <Link to="/customer/account/edit" className="text-blue-600 hover:text-blue-800">
                Edit
              </Link>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-gray-600">Name</p>
                <p className="font-medium">
                  {userData?.firstName} {userData?.lastName}
                </p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Email</p>
                <p className="font-medium">{userData?.email}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Phone</p>
                <p className="font-medium">{userData?.phoneNumber?.value || 'Not provided'}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Date of Birth</p>
                <p className="font-medium">{userData?.dateOfBirth || 'Not provided'}</p>
              </div>
            </div>
          </div>

          {/* Address Information */}
          <div className="mb-8">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-medium">Address</h3>
              <Link to="/customer/account/edit" className="text-blue-600 hover:text-blue-800">
                Edit
              </Link>
            </div>
            <div className="space-y-2">
              <p className="font-medium">{userData?.address?.street}</p>
              <p>
                {userData?.address?.city}, {userData?.address?.zipCode}
              </p>
              <p>{userData?.address?.country}</p>
            </div>
          </div>

          {/* Account Security */}
          <div>
            <h3 className="text-lg font-medium mb-4">Account Security</h3>
            <div className="space-y-4">
              <div className="flex justify-between items-center">
                <div>
                  <p className="font-medium">Password</p>
                  <p className="text-sm text-gray-600">Last changed: Not available</p>
                </div>
                <Link to="/customer/account/security" className="text-blue-600 hover:text-blue-800">
                  Change Password
                </Link>
              </div>
              <div className="flex justify-between items-center">
                <div>
                  <p className="font-medium">Email Address</p>
                  <p className="text-sm text-gray-600">{userData?.email}</p>
                </div>
                <Link to="/customer/account/security" className="text-blue-600 hover:text-blue-800">
                  Change Email
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AccountOverview;
