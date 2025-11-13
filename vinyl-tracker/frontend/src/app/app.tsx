import { Routes, Route } from 'react-router-dom';
import Navbar from '../components/Navbar';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import ProfilePage from '../pages/ProfilePage';
import SubscriptionPage from '../pages/SubscriptionPage';
import AdminVinylsPage from "../pages/admin/AdminVinylsPage";
import AdminUserPage from '../pages/admin/AdminUserPage';
import RequireAdmin from '../components/RequireAdmin';
import VinylsPage from '../pages/VinylsPage';
import CatalogPage from '../pages/CatalogPage';
import WipPage from '../pages/WipPage';

export default function App() {
  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/" element={<VinylsPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/subscription" element={<SubscriptionPage />} />

        {/* новый каталог */}
        <Route path="/catalog" element={<CatalogPage />} />

        {/* "Предложить релиз" пока в разработке */}
        <Route path="/contribute" element={<WipPage title="Страница «Предложить релиз» в разработке" />} />

        <Route
          path="/admin/vinyls"
          element={
            <RequireAdmin>
              <AdminVinylsPage/>
            </RequireAdmin>
          }
        />
        <Route
          path="/admin/users"
          element={
            <RequireAdmin>
              <AdminUserPage />
            </RequireAdmin>
          }
        />
      </Routes>
    </>
  );
}
