import { Navigate, useLocation } from 'react-router-dom';

const AdminGuard = ({ children }: { children: React.ReactNode }) => {
  const location = useLocation();
  const isAdmin = localStorage.getItem('userRole') === 'ADMIN'; // Replace with your auth logic

  if (!isAdmin) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <>{children}</>;
};

export default AdminGuard;
