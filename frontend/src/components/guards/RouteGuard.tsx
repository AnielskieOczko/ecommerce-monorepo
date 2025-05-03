import { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';

interface RouteGuardProps {
  children: ReactNode;
  requiredRoles?: string[];
}

const RouteGuard = ({ children, requiredRoles = [] }: RouteGuardProps) => {
  const { isAuthenticated, user } = useAuth();
  const location = useLocation();

  // Debug logs
  console.log('RouteGuard Check:', {
    path: location.pathname,
    isAuthenticated,
    user,
    userRoles: user?.roles,
    requiredRoles,
  });

  // Check authentication
  if (!isAuthenticated || !user) {
    console.log('RouteGuard - Redirecting to login: Not authenticated');
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Check roles if required
  if (requiredRoles.length > 0) {
    const userRoles = user.roles || [];
    const hasRequiredRole = requiredRoles.some((role) => userRoles.includes(role));

    console.log('RouteGuard - Role check:', {
      userRoles,
      requiredRoles,
      hasRequiredRole,
    });

    if (!hasRequiredRole) {
      console.log('RouteGuard - Redirecting to home: Missing required role');
      return <Navigate to="/" replace />;
    }
  }

  return <>{children}</>;
};

export default RouteGuard;
