import apiClient from './client';
import type { PageResponse } from './employees.api';

export interface Department {
    id: string;
    code: string;
    name: string;
    description: string | null;
    isActive: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface CreateDepartmentRequest {
    code: string;
    name: string;
    description?: string;
    isActive?: boolean;
}

export type UpdateDepartmentRequest = CreateDepartmentRequest;

export const departmentsApi = {
    getAll: (page = 0, size = 100): Promise<PageResponse<Department>> =>
        apiClient
            .get('/employee-service/api/departments', { params: { page, size } })
            .then((res) => res.data),

    getById: (id: string): Promise<Department> =>
        apiClient.get(`/employee-service/api/departments/${id}`).then((res) => res.data),

    create: (data: CreateDepartmentRequest): Promise<Department> =>
        apiClient.post('/employee-service/api/departments', data).then((res) => res.data),

    update: (id: string, data: UpdateDepartmentRequest): Promise<Department> =>
        apiClient.put(`/employee-service/api/departments/${id}`, data).then((res) => res.data),

    delete: (id: string): Promise<void> =>
        apiClient.delete(`/employee-service/api/departments/${id}`).then(() => undefined),
};