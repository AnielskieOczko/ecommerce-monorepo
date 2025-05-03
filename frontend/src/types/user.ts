import { AddressDTO, PhoneNumberDTO, PageRequest, PaginatedResponse } from './common';
import { AuthResponse } from './auth';

// New DTOs matching backend
export interface AccountStatusRequest {
  active: boolean;
}

export interface AdminChangeUserAuthorityRequest {
  authorities: string[];
}

export interface AdminUpdateUserRequest {
  firstName?: string;
  lastName?: string;
  email?: string;
  address?: AddressDTO;
  phoneNumber?: PhoneNumberDTO;
  dateOfBirth?: string;
  authorities?: string[];
  isActive?: boolean;
}

export interface ChangeEmailRequest {
  currentPassword: string;
  newEmail: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface CreateUserRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  address: AddressDTO;
  phoneNumber: PhoneNumberDTO;
  dateOfBirth: string;
  authorities: string[];
}

export interface UpdateBasicDetailsRequest {
  firstName?: string;
  lastName?: string;
  address?: AddressDTO;
  phoneNumber?: PhoneNumberDTO;
  dateOfBirth?: string;
}

export interface UserResponseDTO {
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  address: AddressDTO;
  phoneNumber: PhoneNumberDTO;
  dateOfBirth: string;
  authorities: string[];
  isActive: boolean;
}

export interface UserCounts {
  totalUsers: number;
  activeUsers: number;
  inactiveUsers: number;
  newUsersThisMonth: number;
}

export interface RoleDistribution {
  [key: string]: number; // e.g., { "ROLE_USER": 100, "ROLE_ADMIN": 5 }
}

export interface MonthlyRegistration {
  month: string; // e.g., "2024-01"
  count: number;
}

export interface LoginDistribution {
  last24Hours: number;
  lastWeek: number;
  lastMonth: number;
  inactive30Days: number;
}

export interface CountryStats {
  country: string;
  count: number;
}

export interface VerificationStatus {
  verified: number;
  unverified: number;
}

export interface UserStatistics {
  counts: UserCounts;
  roleDistribution: RoleDistribution;
  registrationsByMonth: MonthlyRegistration[];
  lastLoginDistribution: LoginDistribution;
  topCountries: CountryStats[];
  verificationStatus: VerificationStatus;
}

export interface AdminSearchUsersRequest {
  search?: string;
  isActive?: boolean;
  authority?: string;
  page: number;
  size: number;
  sort: string;
}

// Service interfaces
export interface IUserService {
  // User profile operations
  getProfile(userId: number): Promise<UserResponseDTO>;
  updateBasicDetails(userId: number, data: UpdateBasicDetailsRequest): Promise<UserResponseDTO>;
  changePassword(userId: number, request: ChangePasswordRequest): Promise<void>;
  changeEmail(userId: number, request: ChangeEmailRequest): Promise<AuthResponse>;
  updateAccountStatus(userId: number, request: AccountStatusRequest): Promise<UserResponseDTO>;
  deleteAccount(userId: number): Promise<void>;
}

export interface IAdminUserService {
  // User management
  getAllUsers(params: PageRequest): Promise<PaginatedResponse<UserResponseDTO>>;
  getUserById(userId: number): Promise<UserResponseDTO>;
  createUser(data: CreateUserRequest): Promise<UserResponseDTO>;
  updateUser(userId: number, data: AdminUpdateUserRequest): Promise<UserResponseDTO>;
  deleteUser(userId: number): Promise<void>;

  // User status management
  updateAccountStatus(userId: number, request: AccountStatusRequest): Promise<UserResponseDTO>;

  // Role management
  updateUserAuthorities(
    userId: number,
    request: AdminChangeUserAuthorityRequest
  ): Promise<UserResponseDTO>;
}
