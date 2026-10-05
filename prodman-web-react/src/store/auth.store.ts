import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { authApi } from '../api/auth.api';
import type { AuthResponse, LoginRequest, Role, TenantPlan, TenantStatus } from '../types/auth.types';

export interface User {
    id: string;
    tenantId: string | null;
    username: string;
    email: string;
    role: Role;
}

interface AuthState {
    user: User | null;
    accessToken: string | null;
    refreshToken: string | null;
    tenantId: string | null;
    tenantSlug: string | null;
    tenantStatus: TenantStatus | null;
    tenantPlan: TenantPlan | null;
    isAuthenticated: boolean;
    login: (data: LoginRequest) => Promise<void>;
    logout: () => void;
}

const applyAuthResponse = (res: AuthResponse) => {
    localStorage.setItem('accessToken', res.accessToken);
    localStorage.setItem('refreshToken', res.refreshToken);
    if (res.tenantId) {
        localStorage.setItem('tenantId', res.tenantId);
    } else {
        localStorage.removeItem('tenantId');
    }
};

export const useAuthStore = create<AuthState>()(
    persist(
        (set) => ({
            user: null,
            accessToken: null,
            refreshToken: null,
            tenantId: null,
            tenantSlug: null,
            tenantStatus: null,
            tenantPlan: null,
            isAuthenticated: false,

            login: async (data: LoginRequest) => {
                const res = await authApi.login(data);
                applyAuthResponse(res);

                set({
                    user: {
                        id: res.userId,
                        tenantId: res.tenantId,
                        username: res.username,
                        email: res.email,
                        role: res.role,
                    },
                    accessToken: res.accessToken,
                    refreshToken: res.refreshToken,
                    tenantId: res.tenantId,
                    tenantSlug: res.tenantSlug,
                    tenantStatus: res.tenantStatus,
                    tenantPlan: res.tenantPlan,
                    isAuthenticated: true,
                });
            },

            logout: () => {
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('tenantId');
                set({
                    user: null,
                    accessToken: null,
                    refreshToken: null,
                    tenantId: null,
                    tenantSlug: null,
                    tenantStatus: null,
                    tenantPlan: null,
                    isAuthenticated: false,
                });
            },
        }),
        {
            name: 'auth-storage',
            partialize: (s) => ({
                user: s.user,
                tenantId: s.tenantId,
                tenantSlug: s.tenantSlug,
                tenantStatus: s.tenantStatus,
                tenantPlan: s.tenantPlan,
                isAuthenticated: s.isAuthenticated,
            }),
        }
    )
);