import { Routes, Route } from 'react-router-dom';
import Navbar from '../components/Navbar';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import VinylListPage from '../pages/VinylsPage';
import ProfilePage from '../pages/ProfilePage';
import SubscriptionPage from '../pages/SubscriptionPage';
import AdminPage from "../pages/admin/AdminPage";
import AdminUserTable from '../pages/admin/AdminUserTable';
import RequireAdmin from '../components/RequireAdmin';

export default function App() {
    return (
        <>
            <Navbar />
            <Routes>
                <Route path="/" element={<VinylListPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/profile" element={<ProfilePage />} />
                <Route path="/subscription" element={<SubscriptionPage />} />
              <Route
                path="/admin"
                element={
                  <RequireAdmin>
                    <AdminPage />
                  </RequireAdmin>
                }
              />

              <Route
                path="/admin/users"
                element={
                  <RequireAdmin>
                    <AdminUserTable />
                  </RequireAdmin>
                }
              />
            </Routes>
        </>
    );
}
