import apiClient from './client';

export interface Department {
    id: string;
    name: string;
    color: string;
    createdAt: string;
    updatedAt: string;
}

export interface CreateDepartmentRequest {
    name: string;
    color?: string;
}

export interface UpdateDepartmentRequest {
    name?: string;
    color?: string;
}

export const departmentsApi = {
    getAll: (): Promise<Department[]> =>
        apiClient.get('/employee-service/api/v1/departments').then(res => res.data),

    getById: (id: string): Promise<Department> =>
        apiClient.get(`/employee-service/api/v1/departments/${id}`).then(res => res.data),

    create: (data: CreateDepartmentRequest): Promise<Department> =>
        apiClient.post('/employee-service/api/v1/departments', data).then(res => res.data),

    update: (id: string, data: UpdateDepartmentRequest): Promise<Department> =>
        apiClient.put(`/employee-service/api/v1/departments/${id}`, data).then(res => res.data),

    delete: (id: string): Promise<void> =>
        apiClient.delete(`/employee-service/api/v1/departments/${id}`).then(() => undefined),
};