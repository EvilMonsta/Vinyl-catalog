import { createContext, useContext, useEffect, useState } from 'react';
import { setLogoutCallback } from '../utils/logoutUtil';

export interface AuthUser {
  id: number;
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

function parseJwt(token: string): any {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));

    return JSON.parse(jsonPayload);
  } catch (e) {
    return {};
  }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isInitialized, setIsInitialized] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    const role = localStorage.getItem('role');
    const currentPath = window.location.pathname;

    if (token && username && role) {
      const payload = parseJwt(token);
      console.log('JWT Payload:', payload);

      const isExpired = !payload.exp || payload.exp * 1000 < Date.now();
      const isValidStructure = payload.sub && payload.role;

      if (isExpired || !isValidStructure) {
        logout();
      } else {
        setUser({id: payload.sub,token, username, role });
      }
    } else {
      if (currentPath !== '/login' && currentPath !== '/register') {
        logout();
      }    }

    setLogoutCallback(logout);
    setIsInitialized(true);
  }, []);

  const logout = () => {
    localStorage.clear();
    setUser(null);
    window.location.replace('/login');
  };

  return isInitialized ? (
    <AuthContext.Provider value={{ user, logout }}>
      {children}
    </AuthContext.Provider>
  ) : null;
}

