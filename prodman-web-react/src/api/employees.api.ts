import apiClient from './client';

export type EmployeeStatus =
    | 'AVAILABLE'
    | 'ACTIVE'
    | 'ON_LEAVE'
    | 'FIRED';

export interface Employee {
  id: string;
  code: string;
  firstName: string;
  lastName: string;
  middleName: string | null;
  phone: string; // E.164: +71231231212
  hiredAt: string | null;   // ISO date
  firedAt: string | null;
  status: EmployeeStatus;
  departmentId: string | null;
  departmentName: string | null;
  positionId: string | null;
  positionName: string | null;
  userId: string | null;
  maxConsecutiveHours: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateEmployeeRequest {
  code: string;
  firstName: string;
  lastName: string;
  middleName?: string;
  /** 10 цифр, без +7 (например 1231231212). */
  phone: string;
  hiredAt?: string;
  firedAt?: string;
  departmentId?: string;
  positionId?: string;
  userId?: string;
  maxConsecutiveHours?: number;
}

export type UpdateEmployeeRequest = CreateEmployeeRequest;

/** Backend отдаёт Spring Page. */
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const employeesApi = {
  getAll: (page = 0, size = 20): Promise<PageResponse<Employee>> =>
      apiClient
          .get('/employee-service/api/employees', { params: { page, size } })
          .then((res) => res.data),

  getById: (id: string): Promise<Employee> =>
      apiClient.get(`/employee-service/api/employees/${id}`).then((res) => res.data),

  create: (data: CreateEmployeeRequest): Promise<Employee> =>
      apiClient.post('/employee-service/api/employees', data).then((res) => res.data),

  update: (id: string, data: UpdateEmployeeRequest): Promise<Employee> =>
      apiClient.put(`/employee-service/api/employees/${id}`, data).then((res) => res.data),

  delete: (id: string): Promise<void> =>
      apiClient.delete(`/employee-service/api/employees/${id}`).then(() => undefined),
};