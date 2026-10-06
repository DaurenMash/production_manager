import apiClient from './client';
import type { PageResponse } from './employees.api';

export interface ShiftPattern {
    id: string;
    code: string;
    name: string;
    startTime: string;   // "08:00:00" или "08:00"
    endTime: string;
    crossesMidnight: boolean;
    totalHours: number;
    nightHours: number;
    nightWindowStart: string | null;
    nightWindowEnd: string | null;
    coefficient: number;
    isActive: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface CreateShiftPatternRequest {
    code: string;
    name: string;
    startTime: string;
    endTime: string;
    crossesMidnight?: boolean;
    totalHours: number;
    nightHours?: number;
    nightWindowStart?: string;
    nightWindowEnd?: string;
    coefficient?: number;
    isActive?: boolean;
}

export type UpdateShiftPatternRequest = CreateShiftPatternRequest;

export const shiftPatternsApi = {
    getAll: (page = 0, size = 200): Promise<PageResponse<ShiftPattern>> =>
        apiClient
            .get('/work-calendar/api/shift-patterns', { params: { page, size } })
            .then((res) => res.data),

    getById: (id: string): Promise<ShiftPattern> =>
        apiClient.get(`/work-calendar/api/shift-patterns/${id}`).then((res) => res.data),

    create: (data: CreateShiftPatternRequest): Promise<ShiftPattern> =>
        apiClient.post('/work-calendar/api/shift-patterns', data).then((res) => res.data),

    update: (id: string, data: UpdateShiftPatternRequest): Promise<ShiftPattern> =>
        apiClient.put(`/work-calendar/api/shift-patterns/${id}`, data).then((res) => res.data),

    delete: (id: string): Promise<void> =>
        apiClient.delete(`/work-calendar/api/shift-patterns/${id}`).then(() => undefined),
};