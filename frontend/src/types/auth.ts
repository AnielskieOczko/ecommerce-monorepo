export interface LoginRequest {
    email: string;
    password: string;
}

export interface JwtResponse {
    token: string;
    refreshToken: string;
    type: string;
    id: number;
    email: string;
    roles: string[];
}

export interface AuthResponse {
    success: boolean;
    message: string;
    data: JwtResponse;
}

export interface TokenInfo {
    token: string;
    blacklistedAt: string;
    expiresAt: string;
    blacklistedBy: string;
  }
  
export interface TokenRefreshRequest {
    refreshToken: string;
  }