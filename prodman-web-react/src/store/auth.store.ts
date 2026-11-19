import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { authApi } from '../api/auth.api';
import { tenantsApi } from '../api/tenants.api';
import type { AuthResponse, LoginRequest, Role, TenantPlan, TenantStatus } from '../types/auth.types';

export interface User {
    id: string;
    tenantId: string | null;
    username: string;
    email: string;
    role: Role;
    departmentId: string | null;
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
    isImpersonating: boolean;

    login: (data: LoginRequest) => Promise<void>;
    logout: () => void;
    impersonate: (tenantId: string) => Promise<void>;
    exitImpersonation: () => void;
}

const applyAuthResponse = (res: AuthResponse) => {
    localStorage.setItem('accessToken', res.accessToken);
    localStorage.setItem('refreshToken', res.refreshToken);
    localStorage.setItem('role', res.role);

    if (res.tenantId) {
        localStorage.setItem('tenantId', res.tenantId);
    } else {
        localStorage.removeItem('tenantId');
    }

    if (res.departmentId) {
        localStorage.setItem('departmentId', res.departmentId);
    } else {
        localStorage.removeItem('departmentId');
    }
};

const clearAuthStorage = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('tenantId');
    localStorage.removeItem('role');
    localStorage.removeItem('departmentId');
    localStorage.removeItem('platformAccessToken');
    localStorage.removeItem('platformRefreshToken');
    localStorage.removeItem('platformUserId');
    localStorage.removeItem('platformUsername');
    localStorage.removeItem('platformEmail');
};

export const useAuthStore = create<AuthState>()(
    persist(
        (set, get) => ({
            user: null,
            accessToken: null,
            refreshToken: null,
            tenantId: null,
            tenantSlug: null,
            tenantStatus: null,
            tenantPlan: null,
            isAuthenticated: false,
            isImpersonating: false,

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
                        departmentId: res.departmentId ?? null,
                    },
                    accessToken: res.accessToken,
                    refreshToken: res.refreshToken,
                    tenantId: res.tenantId,
                    tenantSlug: res.tenantSlug,
                    tenantStatus: res.tenantStatus,
                    tenantPlan: res.tenantPlan,
                    isAuthenticated: true,
                    isImpersonating: false,
                });
            },

            logout: () => {
                clearAuthStorage();
                set({
                    user: null,
                    accessToken: null,
                    refreshToken: null,
                    tenantId: null,
                    tenantSlug: null,
                    tenantStatus: null,
                    tenantPlan: null,
                    isAuthenticated: false,
                    isImpersonating: false,
                });
            },

            impersonate: async (tenantId: string) => {
                const state = get();

                // Сохраняем платформенный токен, если ещё не сохраняли
                if (!state.isImpersonating) {
                    localStorage.setItem('platformAccessToken', state.accessToken ?? '');
                    localStorage.setItem('platformRefreshToken', state.refreshToken ?? '');
                    localStorage.setItem('platformUserId', state.user?.id ?? '');
                    localStorage.setItem('platformUsername', state.user?.username ?? '');
                    localStorage.setItem('platformEmail', state.user?.email ?? '');
                }

                const res = await tenantsApi.impersonate(tenantId);
                applyAuthResponse(res);

                set({
                    user: {
                        id: res.userId,
                        tenantId: res.tenantId,
                        username: res.username,
                        email: res.email,
                        role: res.role,
                        departmentId: res.departmentId ?? null,
                    },
                    accessToken: res.accessToken,
                    refreshToken: res.refreshToken,
                    tenantId: res.tenantId,
                    tenantSlug: res.tenantSlug,
                    tenantStatus: res.tenantStatus,
                    tenantPlan: res.tenantPlan,
                    isAuthenticated: true,
                    isImpersonating: true,
                });
            },

            exitImpersonation: () => {
                const platformAccessToken = localStorage.getItem('platformAccessToken');
                const platformRefreshToken = localStorage.getItem('platformRefreshToken');
                const platformUserId = localStorage.getItem('platformUserId');
                const platformUsername = localStorage.getItem('platformUsername');
                const platformEmail = localStorage.getItem('platformEmail');

                if (!platformAccessToken) {
                    // Некуда выходить — выкидываем в logout
                    get().logout();
                    return;
                }

                localStorage.setItem('accessToken', platformAccessToken);
                localStorage.setItem('refreshToken', platformRefreshToken ?? '');
                localStorage.setItem('role', 'PLATFORM_ADMIN');
                localStorage.removeItem('tenantId');
                localStorage.removeItem('departmentId');
                localStorage.removeItem('platformAccessToken');
                localStorage.removeItem('platformRefreshToken');
                localStorage.removeItem('platformUserId');
                localStorage.removeItem('platformUsername');
                localStorage.removeItem('platformEmail');

                set({
                    user: {
                        id: platformUserId ?? '',
                        tenantId: null,
                        username: platformUsername ?? '',
                        email: platformEmail ?? '',
                        role: 'PLATFORM_ADMIN',
                        departmentId: null,
                    },
                    accessToken: platformAccessToken,
                    refreshToken: platformRefreshToken,
                    tenantId: null,
                    tenantSlug: null,
                    tenantStatus: null,
                    tenantPlan: null,
                    isAuthenticated: true,
                    isImpersonating: false,
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
                isImpersonating: s.isImpersonating,
            }),
        }
    )
);