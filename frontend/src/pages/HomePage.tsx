import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import BlogSection from '../components/BlogSection';
import Hero from '../components/Hero';
import ProductSection from '../components/ProductSection';
import Reviews from '../components/Reviews';

const HomePage = () => {
  const { user } = useAuth();

  // Check if user is an admin and redirect to admin dashboard
  if (user?.roles.includes('ROLE_ADMIN')) {
    return <Navigate to="/admin" replace />;
  }

  return (
    <>
      <Hero />
      <ProductSection title="Kosmetyki z Serii Białej" bgColor="bg-white" />
      <ProductSection title="Kosmetyki z Serii Białej" bgColor="bg-white" />
      <BlogSection />
      <Reviews />
    </>
  );
};

export default HomePage;
