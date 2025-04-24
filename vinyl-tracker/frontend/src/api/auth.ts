import instance from './axios';


export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  role: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  roleId?: number;
}

export interface UserDto {
  id: number;
  username: string;
  email: string;
  roleId: number;
}

export const login = async (data: LoginRequest): Promise<LoginResponse> => {
  const response = await instance.post('/api/auth/login', data);
  return response.data;
};

export const registerUser = async (data: RegisterRequest): Promise<UserDto> => {
  const response = await instance.post('/api/auth/register', data);
  return response.data;
};

export const getMyProfile = async (): Promise<UserDto> => {
  const response = await instance.get('/api/user/me');
  return response.data;
};
