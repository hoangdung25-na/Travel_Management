import { createContext, useState, useEffect } from 'react';
import apiClient from '../services/apiClient';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUser = async () => {
      const token = localStorage.getItem('token');
      if (token) {
        try {
          const response = await apiClient.get('/auth/me');
          setUser(response.data.data);
        } catch (error) {
          console.error('Failed to fetch user:', error);
          localStorage.removeItem('token');
        }
      }
      setLoading(false);
    };

    fetchUser();
  }, []);

  const login = async (email, password) => {
    const response = await apiClient.post('/auth/login', { username: email, password });
    const { accessToken } = response.data.data;
    localStorage.setItem('token', accessToken);
    
    // Fetch user profile immediately after login
    const profileResponse = await apiClient.get('/auth/me', {
        headers: {
            'X-User-Id': accessToken // Pass header explicitly here to ensure it works immediately after login
        }
    });
    setUser(profileResponse.data.data);
  };

  const register = async (userData) => {
    await apiClient.post('/auth/register', userData);
  };

  const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, setUser, login, register, logout, loading }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};
