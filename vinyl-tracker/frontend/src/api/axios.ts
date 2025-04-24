import axios from 'axios';
import { triggerLogout } from '../utils/logoutUtil';

const instance = axios.create({
  baseURL: 'http://localhost:8080',
});

instance.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

instance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      triggerLogout();
    }
    return Promise.reject(error);
  }
);

export default instance;
