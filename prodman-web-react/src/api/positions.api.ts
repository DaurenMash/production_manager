import apiClient from './client';
import type { PageResponse } from './employees.api';

export interface Position {
    id: string;
    departmentId: string;
    departmentName: string | null;
    code: string;
    name: string;
    description: string | null;
    isActive: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface CreatePositionRequest {
    departmentId: string;
    code: string;
    name: string;
    description?: string;
    isActive?: boolean;
}

export type UpdatePositionRequest = CreatePositionRequest;

export const positionsApi = {
    getAll: (page = 0, size = 200, departmentId?: string): Promise<PageResponse<Position>> =>
        apiClient
            .get('/employee-service/api/positions', {
                params: { page, size, ...(departmentId ? { departmentId } : {}) },
            })
            .then((res) => res.data),

    getById: (id: string): Promise<Position> =>
        apiClient.get(`/employee-service/api/positions/${id}`).then((res) => res.data),

    create: (data: CreatePositionRequest): Promise<Position> =>
        apiClient.post('/employee-service/api/positions', data).then((res) => res.data),

    update: (id: string, data: UpdatePositionRequest): Promise<Position> =>
        apiClient.put(`/employee-service/api/positions/${id}`, data).then((res) => res.data),

    delete: (id: string): Promise<void> =>
        apiClient.delete(`/employee-service/api/positions/${id}`).then(() => undefined),
};