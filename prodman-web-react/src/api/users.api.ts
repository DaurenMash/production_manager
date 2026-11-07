import apiClient from './client';
import type { PageResponse } from './employees.api';
import type { Role } from '../types/auth.types';

export interface AppUser {
    id: string;
    tenantId: string;
    username: string;
    email: string;
    role: Role;
    departmentId: string | null;
    enabled: boolean;
    createdAt: string;
    updatedAt: string;
    tempRoles: UserTempRole[];
    createdBy: string | null;
    createdByRole: Role | null;
}

export interface UserTempRole {
    id: string;
    role: Role;
    validFrom: string;
    validUntil: string | null;
}

export interface CreateUserRequest {
    username: string;
    email: string;
    password: string;
    role: Role;
    departmentId?: string | null;
}

export const usersApi = {
    getAll: (page = 0, size = 100): Promise<PageResponse<AppUser>> =>
        apiClient
            .get('/user-service/api/v1/users', { params: { page, size } })
            .then((res) => res.data),

    create: (data: CreateUserRequest): Promise<AppUser> =>
        apiClient.post('/user-service/api/v1/users', data).then((res) => res.data),

    delete: (id: string): Promise<void> =>
        apiClient.delete(`/user-service/api/v1/users/${id}`).then(() => undefined),

    assignTempRole: (userId: string, role: Role, validUntil: string | null): Promise<AppUser> =>
        apiClient
            .post(`/user-service/api/v1/users/${userId}/temp-roles`, { role, validUntil })
            .then((res) => res.data),

    revokeTempRole: (userId: string, role: Role): Promise<void> =>
        apiClient
            .delete(`/user-service/api/v1/users/${userId}/temp-roles/${role}`)
            .then(() => undefined),
};