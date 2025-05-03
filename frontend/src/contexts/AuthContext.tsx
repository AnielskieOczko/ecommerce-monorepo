import { createContext, useState, useEffect, ReactNode, useContext } from 'react';
import { authService } from '../services/authService';
import { JwtResponse } from '../types/auth';
import { UserResponseDTO } from '../types/user';

interface AuthUser extends JwtResponse {
  firstName?: string;
  lastName?: string;
}

interface AuthContextType {
  isAuthenticated: boolean;
  user: AuthUser | null;
  login: (response: JwtResponse) => void;
  logout: () => Promise<void>;
  updateUser: (userData: Partial<AuthUser>) => void;
}

export const AuthContext = createContext<AuthContextType | null>(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const login = (response: JwtResponse) => {
    localStorage.setItem('token', response.token);
    localStorage.setItem('refreshToken', response.refreshToken);
    setIsAuthenticated(true);
    setUser(response);
  };

  const logout = async () => {
    await authService.logout();
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    setIsAuthenticated(false);
    setUser(null);
  };

  const updateUser = (userData: Partial<AuthUser>) => {
    if (user) {
      const updatedUser = { ...user, ...userData };
      setUser(updatedUser);
    }
  };

  useEffect(() => {
    const initializeAuth = () => {
      const token = localStorage.getItem('token');
      if (token) {
        try {
          const payload = JSON.parse(atob(token.split('.')[1]));
          console.log('Full JWT Payload:', payload);

          setIsAuthenticated(true);
          setUser({
            token,
            email: payload.username || payload.sub,
            id: parseInt(payload.sub),
            roles: payload.authorities || [],
            refreshToken: localStorage.getItem('refreshToken') || '',
            type: 'Bearer',
          });

          console.log('Auth state initialized:', {
            isAuthenticated: true,
            roles: payload.authorities,
          });
        } catch (error) {
          console.error('Error parsing token:', error);
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
          setIsAuthenticated(false);
          setUser(null);
        }
      } else {
        setIsAuthenticated(false);
        setUser(null);
      }
      setIsLoading(false);
    };

    initializeAuth();
  }, []);

  if (isLoading) {
    return <div>Loading...</div>;
  }

  return (
    <AuthContext.Provider value={{ isAuthenticated, user, login, logout, updateUser }}>
      {children}
    </AuthContext.Provider>
  );
};
