import apiClient from './client';
import type { PageResponse } from './employees.api';

export interface Workstation {
    id: string;
    departmentId: string;
    requiredQualificationId: string | null;
    code: string;
    name: string;
    description: string | null;
    isActive: boolean;
    createdBy: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface CreateWorkstationRequest {
    departmentId: string;
    requiredQualificationId?: string;
    code: string;
    name: string;
    description?: string;
    isActive?: boolean;
}

export type UpdateWorkstationRequest = CreateWorkstationRequest;

export const workstationsApi = {
    getAll: (page = 0, size = 200, departmentId?: string): Promise<PageResponse<Workstation>> =>
        apiClient
            .get('/workstation/api/workstations', {
                params: { page, size, ...(departmentId ? { departmentId } : {}) },
            })
            .then((res) => res.data),

    getById: (id: string): Promise<Workstation> =>
        apiClient.get(`/workstation/api/workstations/${id}`).then((res) => res.data),

    create: (data: CreateWorkstationRequest): Promise<Workstation> =>
        apiClient.post('/workstation/api/workstations', data).then((res) => res.data),

    update: (id: string, data: UpdateWorkstationRequest): Promise<Workstation> =>
        apiClient.put(`/workstation/api/workstations/${id}`, data).then((res) => res.data),

    delete: (id: string): Promise<void> =>
        apiClient.delete(`/workstation/api/workstations/${id}`).then(() => undefined),
};