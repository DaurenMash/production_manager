import apiClient from './client';

export type ShiftSlotStatus = 'OPEN' | 'FILLED' | 'CANCELLED';

export interface ShiftSlot {
    id: string;
    date: string;
    workstationId: string;
    workstationName: string | null;
    shiftPatternId: string;
    shiftPatternName: string | null;
    requiredQualificationId: string | null;
    employeeId: string | null;
    employeeFullName: string | null;
    employeeDepartmentId: string | null;
    plannedHours: number;
    plannedNightHours: number;
    actualHours: number | null;
    actualNightHours: number | null;
    coefficientOverride: number | null;
    status: ShiftSlotStatus;
    comment: string | null;
    overridden: boolean;
    overrideReason: string | null;
    createdBy: string | null;
    updatedBy: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface WorkstationDayStatus {
    id: string;
    workstationId: string;
    date: string;
    isWorking: boolean;
    note: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface WorkstationShift {
    id: string;
    workstationId: string;
    date: string;
    shiftPatternId: string;
    shiftPatternName: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface DayBoard {
    date: string;
    workstations: WorkstationBoard[];
}

export interface WorkstationBoard {
    workstationId: string;
    workstationName: string | null;
    departmentId: string | null;
    requiredQualificationId: string | null;
    isWorking: boolean;
    shifts: ShiftBoard[];
}

export interface ShiftBoard {
    workstationShiftId: string;
    shiftPatternId: string;
    shiftPatternName: string | null;
    totalHours: number | null;
    nightHours: number | null;
    slots: ShiftSlot[];
}

export interface CreateShiftSlotRequest {
    date: string;
    workstationId: string;
    shiftPatternId: string;
    requiredQualificationId?: string;
}

export interface AssignEmployeeToSlotRequest {
    employeeId: string;
    force?: boolean;
    overrideReason?: string;
    comment?: string;
}

export interface UpdateShiftSlotRequest {
    requiredQualificationId?: string;
    actualHours?: number;
    actualNightHours?: number;
    coefficientOverride?: number;
    comment?: string;
}

export interface SetWorkstationDayStatusRequest {
    workstationId: string;
    date: string;
    isWorking: boolean;
    note?: string;
}

export interface AddWorkstationShiftRequest {
    workstationId: string;
    date: string;
    shiftPatternId: string;
}

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

export const workCalendarApi = {
    // Day board — агрегат «день → станки → смены → слоты»
    getDayBoard: (date: string): Promise<DayBoard> =>
        apiClient
            .get('/work-calendar/api/day-board', { params: { date } })
            .then((res) => res.data),

    // Workstation day status
    setWorkstationDayStatus: (data: SetWorkstationDayStatusRequest): Promise<WorkstationDayStatus> =>
        apiClient
            .post('/work-calendar/api/workstation-day-status', data)
            .then((res) => res.data),

    deleteWorkstationDayStatus: (id: string): Promise<void> =>
        apiClient
            .delete(`/work-calendar/api/workstation-day-status/${id}`)
            .then(() => undefined),

    // Workstation shifts — какие смены выбраны для станка в день
    addWorkstationShift: (data: AddWorkstationShiftRequest): Promise<WorkstationShift> =>
        apiClient
            .post('/work-calendar/api/workstation-shifts', data)
            .then((res) => res.data),

    deleteWorkstationShift: (id: string): Promise<void> =>
        apiClient
            .delete(`/work-calendar/api/workstation-shifts/${id}`)
            .then(() => undefined),

    // Shift slots
    listSlots: (date: string): Promise<ShiftSlot[]> =>
        apiClient
            .get('/work-calendar/api/shift-slots', { params: { date } })
            .then((res) => res.data),

    createSlot: (data: CreateShiftSlotRequest): Promise<ShiftSlot> =>
        apiClient
            .post('/work-calendar/api/shift-slots', data)
            .then((res) => res.data),

    updateSlot: (id: string, data: UpdateShiftSlotRequest): Promise<ShiftSlot> =>
        apiClient
            .put(`/work-calendar/api/shift-slots/${id}`, data)
            .then((res) => res.data),

    assignEmployee: (slotId: string, data: AssignEmployeeToSlotRequest): Promise<ShiftSlot> =>
        apiClient
            .post(`/work-calendar/api/shift-slots/${slotId}/assign`, data)
            .then((res) => res.data),

    unassignEmployee: (slotId: string): Promise<ShiftSlot> =>
        apiClient
            .post(`/work-calendar/api/shift-slots/${slotId}/unassign`)
            .then((res) => res.data),

    deleteSlot: (id: string): Promise<void> =>
        apiClient
            .delete(`/work-calendar/api/shift-slots/${id}`)
            .then(() => undefined),

    // Reports
    reportByEmployee: (from: string, to: string): Promise<EmployeeHoursReportRow[]> =>
        apiClient
            .get('/work-calendar/api/reports/hours/by-employee', { params: { from, to } })
            .then((res) => res.data),

    reportByDepartment: (from: string, to: string): Promise<DepartmentHoursReportRow[]> =>
        apiClient
            .get('/work-calendar/api/reports/hours/by-department', { params: { from, to } })
            .then((res) => res.data),
};