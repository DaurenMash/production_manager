import apiClient from './client';
import type { PageResponse } from './employees.api';

export interface Qualification {
    id: string;
    positionId: string;
    positionName: string | null;
    code: string;
    name: string;
    description: string | null;
    isActive: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface CreateQualificationRequest {
    positionId: string;
    code: string;
    name: string;
    description?: string;
    isActive?: boolean;
}

export type UpdateQualificationRequest = CreateQualificationRequest;

export interface EmployeeQualification {
    id: string;
    employeeId: string;
    qualificationId: string;
    qualificationCode: string;
    qualificationName: string;
    level: number;
    assignedAt: string;
    assignedBy: string | null;
    notes: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface AssignQualificationRequest {
    qualificationId: string;
    level?: number;
    assignedBy?: string;
    notes?: string;
}

export const qualificationsApi = {
    getAll: (page = 0, size = 200, positionId?: string): Promise<PageResponse<Qualification>> =>
        apiClient
            .get('/employee-service/api/qualifications', {
                params: { page, size, ...(positionId ? { positionId } : {}) },
            })
            .then((res) => res.data),

    getById: (id: string): Promise<Qualification> =>
        apiClient.get(`/employee-service/api/qualifications/${id}`).then((res) => res.data),

    create: (data: CreateQualificationRequest): Promise<Qualification> =>
        apiClient.post('/employee-service/api/qualifications', data).then((res) => res.data),

    update: (id: string, data: UpdateQualificationRequest): Promise<Qualification> =>
        apiClient.put(`/employee-service/api/qualifications/${id}`, data).then((res) => res.data),

    delete: (id: string): Promise<void> =>
        apiClient.delete(`/employee-service/api/qualifications/${id}`).then(() => undefined),

    // Employee ↔ Qualification
    listForEmployee: (employeeId: string): Promise<EmployeeQualification[]> =>
        apiClient
            .get(`/employee-service/api/employees/${employeeId}/qualifications`)
            .then((res) => res.data),

    assign: (employeeId: string, data: AssignQualificationRequest): Promise<EmployeeQualification> =>
        apiClient
            .post(`/employee-service/api/employees/${employeeId}/qualifications`, data)
            .then((res) => res.data),

    revoke: (employeeId: string, qualificationId: string): Promise<void> =>
        apiClient
            .delete(`/employee-service/api/employees/${employeeId}/qualifications/${qualificationId}`)
            .then(() => undefined),
};