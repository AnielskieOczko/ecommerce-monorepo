import { Outlet } from 'react-router-dom';
import GlobalLoader from '../components/GlobalLoader';
import Header from '../components/Header';
import Footer from '../components/Footer';

const PublicLayout = () => {
  return (
    <div className="min-h-screen bg-white">
      <GlobalLoader />
      <Header />
      <main>
        <Outlet />
      </main>
      <Footer />
    </div>
  );
};

export default PublicLayout;
