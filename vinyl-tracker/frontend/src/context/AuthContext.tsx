import { createContext, useContext, useEffect, useState } from 'react';

export interface AuthUser {
  username: string;
  role: string;
  token: string;
}

interface AuthContextType {
  user: AuthUser | null;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const useAuth = () => useContext(AuthContext);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    const role = localStorage.getItem('role');
    if (token && username && role) {
      setUser({ token, username, role });
    }
  }, []);

  const logout = () => {
    localStorage.clear();
    setUser(null);
    window.location.href = '/login';
  };

  return (
    <AuthContext.Provider value={{ user, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
