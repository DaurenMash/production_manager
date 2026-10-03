import apiClient from './client';
import type {
  AuthResponse,
  LoginRequest,
  RegisterTenantRequest,
  User,
} from '../types/auth.types';

export const authApi = {
  login: (data: LoginRequest): Promise<AuthResponse> =>
      apiClient.post('/user-service/api/v1/auth/login', data).then((res) => res.data),

  registerTenant: (data: RegisterTenantRequest): Promise<User> =>
      apiClient
          .post('/user-service/api/v1/auth/register-tenant', data)
          .then((res) => res.data),

  refresh: (refreshToken: string): Promise<AuthResponse> =>
      apiClient
          .post('/user-service/api/v1/auth/refresh', { refreshToken })
          .then((res) => res.data),

  logout: (): Promise<void> =>
      apiClient.post('/user-service/api/v1/auth/logout').then(() => undefined),
};