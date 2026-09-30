import apiClient from './client';

export interface Position {
    id: string;
    name: string;
    color: string;
    createdAt: string;
    updatedAt: string;
}

export interface CreatePositionRequest {
    name: string;
    color?: string;
}

export interface UpdatePositionRequest {
    name?: string;
    color?: string;
}

export const positionsApi = {
    getAll: (): Promise<Position[]> =>
        apiClient.get('/employee-service/api/v1/positions').then(res => res.data),

    getById: (id: string): Promise<Position> =>
        apiClient.get(`/employee-service/api/v1/positions/${id}`).then(res => res.data),

    create: (data: CreatePositionRequest): Promise<Position> =>
        apiClient.post('/employee-service/api/v1/positions', data).then(res => res.data),

    update: (id: string, data: UpdatePositionRequest): Promise<Position> =>
        apiClient.put(`/employee-service/api/v1/positions/${id}`, data).then(res => res.data),

    delete: (id: string): Promise<void> =>
        apiClient.delete(`/employee-service/api/v1/positions/${id}`).then(() => undefined),
};