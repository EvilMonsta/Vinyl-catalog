import { useAuth } from '../context/AuthContext';
import { Navigate } from 'react-router-dom';
import { ReactNode } from 'react';

export default function RequireAdmin({ children }: { children: ReactNode }) {
  const auth = useAuth();

  if (!auth?.user) {
    return <Navigate to="/" replace />;
  }

  if (auth.user.role !== 'ADMIN') {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
}
