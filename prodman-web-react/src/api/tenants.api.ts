import apiClient from './client';
import type { PageResponse } from './employees.api';
import type { TenantPlan, TenantStatus } from '../types/auth.types';

export interface Tenant {
    id: string;
    name: string;
    slug: string;
    status: TenantStatus;
    plan: TenantPlan;
    trialEndsAt: string | null;
    subscriptionEndsAt: string | null;
    createdAt: string;
}

export const tenantsApi = {
    getAll: (page = 0, size = 50): Promise<PageResponse<Tenant>> =>
        apiClient
            .get('/user-service/api/v1/tenants', { params: { page, size } })
            .then((res) => res.data),
};