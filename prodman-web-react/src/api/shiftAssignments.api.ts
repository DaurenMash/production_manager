import apiClient from './client';
import type { PageResponse } from './employees.api';

export type ShiftAssignmentStatus = 'PLANNED' | 'CONFIRMED' | 'DONE' | 'CANCELLED';

export interface ShiftAssignment {
    id: string;
    date: string;
    employeeId: string;
    employeeFullName: string | null;
    employeeDepartmentId: string | null;
    workstationId: string;
    workstationName: string | null;
    shiftPatternId: string;
    shiftPatternName: string | null;
    plannedHours: number;
    plannedNightHours: number;
    actualHours: number | null;
    actualNightHours: number | null;
    coefficientOverride: number | null;
    status: ShiftAssignmentStatus;
    comment: string | null;
    overridden: boolean;
    overrideReason: string | null;
    createdBy: string | null;
    updatedBy: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface CreateShiftAssignmentRequest {
    date: string; // YYYY-MM-DD
    employeeId: string;
    workstationId: string;
    shiftPatternId: string;
    actualHours?: number;
    actualNightHours?: number;
    coefficientOverride?: number;
    comment?: string;
    force?: boolean;
    overrideReason?: string;
}

export type UpdateShiftAssignmentRequest = CreateShiftAssignmentRequest & {
    status?: ShiftAssignmentStatus;
};

export interface EmployeeHoursReportRow {
    employeeId: string;
    employeeFullName: string | null;
    employeeDepartmentId: string | null;
    totalHours: number;
    nightHours: number;
    assignmentsCount: number;
}

export interface DepartmentHoursReportRow {
    departmentId: string | null;
    totalHours: number;
    nightHours: number;
    assignmentsCount: number;
}

export const shiftAssignmentsApi = {
    getAll: (page = 0, size = 200): Promise<PageResponse<ShiftAssignment>> =>
        apiClient
            .get('/work-calendar/api/shift-assignments', { params: { page, size } })
            .then((res) => res.data),

    getById: (id: string): Promise<ShiftAssignment> =>
        apiClient.get(`/work-calendar/api/shift-assignments/${id}`).then((res) => res.data),

    listByRange: (from: string, to: string): Promise<ShiftAssignment[]> =>
        apiClient
            .get('/work-calendar/api/shift-assignments/range', { params: { from, to } })
            .then((res) => res.data),

    listByEmployee: (employeeId: string, from: string, to: string): Promise<ShiftAssignment[]> =>
        apiClient
            .get(`/work-calendar/api/shift-assignments/employee/${employeeId}`, {
                params: { from, to },
            })
            .then((res) => res.data),

    create: (data: CreateShiftAssignmentRequest): Promise<ShiftAssignment> =>
        apiClient.post('/work-calendar/api/shift-assignments', data).then((res) => res.data),

    update: (id: string, data: UpdateShiftAssignmentRequest): Promise<ShiftAssignment> =>
        apiClient.put(`/work-calendar/api/shift-assignments/${id}`, data).then((res) => res.data),

    delete: (id: string): Promise<void> =>
        apiClient.delete(`/work-calendar/api/shift-assignments/${id}`).then(() => undefined),

    reportByEmployee: (from: string, to: string): Promise<EmployeeHoursReportRow[]> =>
        apiClient
            .get('/work-calendar/api/reports/hours/by-employee', { params: { from, to } })
            .then((res) => res.data),

    reportByDepartment: (from: string, to: string): Promise<DepartmentHoursReportRow[]> =>
        apiClient
            .get('/work-calendar/api/reports/hours/by-department', { params: { from, to } })
            .then((res) => res.data),
};