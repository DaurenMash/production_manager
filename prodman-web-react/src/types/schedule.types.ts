/**
 * Зеркало ScheduleResponse.java (work-calendar).
 * GET /work-calendar/api/reports/schedule?from=&to=&departmentId=
 */

export type ScheduleCellKind =
    | 'SHIFT'
    | 'VACATION'
    | 'SICK_LEAVE'
    | 'UNAVAILABLE'
    | 'FIRED'
    | 'DAY_OFF'
    | 'EMPTY';

export interface ScheduleCell {
    date: string;                    // ISO yyyy-MM-dd
    kind: ScheduleCellKind;
    shiftCode: string | null;
    shiftName: string | null;
    color: string | null;            // HEX из ShiftPattern.color
    isDayOffWork: boolean | null;
    totalHours: number | null;
    nightHours: number | null;
    workstationId: string | null;
    workstationName: string | null;
}

export interface ScheduleRow {
    employeeId: string;
    employeeFullName: string | null;
    departmentId: string | null;
    employeeStatus: string | null;
    days: ScheduleCell[];
}

export interface ScheduleResponse {
    from: string;
    to: string;
    employees: ScheduleRow[];
}