import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { adminUserService } from '../../../services/adminUserService';
import { authorityService } from '../../../services/authorityService';
import { UserResponseDTO, AdminSearchUsersRequest } from '../../../types/user';
import { PaginatedResponse, SortDirection } from '../../../types/common';
import { toast } from 'react-toastify';
import debounce from 'lodash/debounce';

// Add sort field type
type UserSortField = 'id' | 'firstName' | 'lastName' | 'email' | 'isActive' | 'authorities';

const UserList = () => {
  const [users, setUsers] = useState<PaginatedResponse<UserResponseDTO> | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [authorities, setAuthorities] = useState<string[]>([]);
  const [searchRequest, setSearchRequest] = useState<AdminSearchUsersRequest>({
    page: 1,
    size: 10,
    sort: 'id:asc',
    search: '',
    isActive: undefined,
    authority: '',
  });
  const [searchInput, setSearchInput] = useState('');

  // Add sort state
  const [sortField, setSortField] = useState<UserSortField>('id');
  const [sortDirection, setSortDirection] = useState<SortDirection>('asc');

  // Add sort handler
  const handleSort = (field: UserSortField) => {
    setSortField(field);
    setSortDirection((prev) => (field === sortField ? (prev === 'asc' ? 'desc' : 'asc') : 'asc'));
    setSearchRequest((prev) => ({
      ...prev,
      sort: `${field}:${field === sortField ? (sortDirection === 'asc' ? 'desc' : 'asc') : 'asc'}`,
    }));
  };

  // Add sort icon helper
  const getSortIcon = (field: UserSortField) => {
    if (field !== sortField) return '↕️';
    return sortDirection === 'asc' ? '↑' : '↓';
  };

  useEffect(() => {
    loadUsers();
  }, [searchRequest]);

  useEffect(() => {
    loadAuthorities();
  }, []);

  const loadAuthorities = async () => {
    try {
      const authorityNames = await authorityService.getAuthorityNames();
      setAuthorities(Array.from(authorityNames));
    } catch (error) {
      console.error('Error loading authorities:', error);
      toast.error('Failed to load authority options');
    }
  };

  const loadUsers = async () => {
    try {
      setLoading(true);
      setError(null);
      console.log('Loading users with request:', searchRequest);
      const response = await adminUserService.getAllUsers(searchRequest);
      console.log('Received users response:', response);
      setUsers(response);
    } catch (error) {
      console.error('Error loading users:', error);
      const errorMessage = error instanceof Error ? error.message : 'Failed to load users';
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleSearchChange = debounce((value: string) => {
    setSearchRequest((prev) => ({ ...prev, search: value, page: 1 }));
  }, 300);

  const handleStatusFilterChange = (value: string) => {
    setSearchRequest((prev) => ({
      ...prev,
      isActive: value === '' ? undefined : value === 'active',
      page: 1,
    }));
  };

  const handleRoleFilterChange = (value: string) => {
    setSearchRequest((prev) => ({ ...prev, authority: value, page: 1 }));
  };

  const handlePageChange = (newPage: number) => {
    setSearchRequest((prev) => ({ ...prev, page: newPage }));
  };

  const handlePageSizeChange = (newSize: number) => {
    setSearchRequest((prev) => ({ ...prev, size: newSize, page: 1 }));
  };

  const handleStatusChange = async (userId: number, isActive: boolean) => {
    try {
      setLoading(true);
      await adminUserService.updateAccountStatus(userId, { active: !isActive });
      await loadUsers();
      toast.success('User status updated successfully');
    } catch (error) {
      toast.error('Failed to update user status');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (userId: number) => {
    if (window.confirm('Are you sure you want to delete this user?')) {
      try {
        setLoading(true);
        await adminUserService.deleteUser(userId);
        await loadUsers();
        toast.success('User deleted successfully');
      } catch (error) {
        toast.error('Failed to delete user');
      } finally {
        setLoading(false);
      }
    }
  };

  if (loading && !users?.content?.length) {
    return <div>Loading users...</div>;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-semibold">User Management</h1>
        <Link
          to="/admin/users/create"
          className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
        >
          Create User
        </Link>
      </div>

      {/* Filters */}
      <div className="bg-white shadow-md rounded-lg p-4 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {/* Search Input */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Search</label>
            <input
              type="text"
              placeholder="Search users..."
              value={searchInput}
              onChange={(e) => {
                setSearchInput(e.target.value);
                handleSearchChange(e.target.value);
              }}
              className="w-full p-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* Status Filter */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
            <select
              onChange={(e) => handleStatusFilterChange(e.target.value)}
              className="w-full p-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">All Status</option>
              <option value="active">Active</option>
              <option value="inactive">Inactive</option>
            </select>
          </div>

          {/* Role Filter */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Role</label>
            <select
              onChange={(e) => handleRoleFilterChange(e.target.value)}
              className="w-full p-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
              value={searchRequest.authority || ''}
            >
              <option value="">All Roles</option>
              {authorities.map((authority) => (
                <option key={authority} value={authority}>
                  {authority.replace('ROLE_', '')}
                </option>
              ))}
            </select>
          </div>

          {/* Page Size */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Items per page</label>
            <select
              value={searchRequest.size}
              onChange={(e) => handlePageSizeChange(Number(e.target.value))}
              className="w-full p-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value={10}>10</option>
              <option value={25}>25</option>
              <option value={50}>50</option>
            </select>
          </div>
        </div>
      </div>

      {error ? (
        <div className="text-red-600 p-4">
          <p>Error: {error}</p>
          <button
            onClick={loadUsers}
            className="mt-2 bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
          >
            Retry
          </button>
        </div>
      ) : !users?.content?.length ? (
        <div className="bg-white shadow-md rounded-lg p-6 text-center">No users found</div>
      ) : (
        <>
          <div className="bg-white shadow-md rounded-lg overflow-hidden">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                    onClick={() => handleSort('id')}
                  >
                    ID {getSortIcon('id')}
                  </th>
                  <th
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                    onClick={() => handleSort('firstName')}
                  >
                    Name {getSortIcon('firstName')}
                  </th>
                  <th
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                    onClick={() => handleSort('email')}
                  >
                    Email {getSortIcon('email')}
                  </th>
                  <th
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                    onClick={() => handleSort('isActive')}
                  >
                    Status {getSortIcon('isActive')}
                  </th>
                  <th
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                    onClick={() => handleSort('authorities')}
                  >
                    Roles {getSortIcon('authorities')}
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {users?.content.map((user) => (
                  <tr key={user.userId}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {user.userId}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">
                        {user.firstName} {user.lastName}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {user.email}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span
                        className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                          user.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                        }`}
                      >
                        {user.isActive ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {user.authorities.map((auth) => auth.replace('ROLE_', '')).join(', ')}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <div className="flex space-x-2">
                        <Link
                          to={`/admin/users/${user.userId}/edit`}
                          className="text-indigo-600 hover:text-indigo-900"
                        >
                          Edit
                        </Link>
                        <button
                          onClick={() => handleStatusChange(user.userId, user.isActive)}
                          className={`${
                            user.isActive
                              ? 'text-red-600 hover:text-red-900'
                              : 'text-green-600 hover:text-green-900'
                          }`}
                        >
                          {user.isActive ? 'Deactivate' : 'Activate'}
                        </button>
                        <button
                          onClick={() => handleDelete(user.userId)}
                          className="text-red-600 hover:text-red-900"
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {users && (
            <div className="flex justify-between items-center mt-4">
              <div className="text-sm text-gray-700">
                Showing {(searchRequest.page - 1) * searchRequest.size + 1} to{' '}
                {Math.min(searchRequest.page * searchRequest.size, users.totalElements)} of{' '}
                {users.totalElements} results
              </div>
              <div className="flex space-x-2">
                <button
                  onClick={() => handlePageChange(searchRequest.page - 1)}
                  disabled={searchRequest.page === 1}
                  className="px-3 py-1 border rounded-md disabled:opacity-50"
                >
                  Previous
                </button>
                <button
                  onClick={() => handlePageChange(searchRequest.page + 1)}
                  disabled={searchRequest.page >= users.totalPages}
                  className="px-3 py-1 border rounded-md disabled:opacity-50"
                >
                  Next
                </button>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default UserList;
