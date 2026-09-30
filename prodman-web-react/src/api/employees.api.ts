import apiClient from './client';

export interface PositionInfo {
  id: string;
  name: string;
  color: string;
}

export interface DepartmentInfo {
  id: string;
  name: string;
  color: string;
}

export type EmployeeStatus =
    | 'AVAILABLE'
    | 'BUSY'
    | 'VACATION'
    | 'SICK_LEAVE'
    | 'UNAVAILABLE';

export interface Employee {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  department: DepartmentInfo | null;
  position: PositionInfo | null;
  status: EmployeeStatus;
  maxConsecutiveHours: number;
  userId: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateEmployeeRequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
  departmentId?: string;
  positionId?: string;
  maxConsecutiveHours?: number;
  userId?: string;
}

export type UpdateEmployeeRequest = Partial<CreateEmployeeRequest> & {
  status?: EmployeeStatus;
};

export const employeesApi = {
  getAll: (): Promise<Employee[]> =>
      apiClient.get('/employee-service/api/v1/employees').then(res => res.data),

  getById: (id: string): Promise<Employee> =>
      apiClient.get(`/employee-service/api/v1/employees/${id}`).then(res => res.data),

  getByEmail: (email: string): Promise<Employee> =>
      apiClient.get(`/employee-service/api/v1/employees/email/${email}`).then(res => res.data),

  getByStatus: (status: EmployeeStatus): Promise<Employee[]> =>
      apiClient.get(`/employee-service/api/v1/employees/status/${status}`).then(res => res.data),

  create: (data: CreateEmployeeRequest): Promise<Employee> =>
      apiClient.post('/employee-service/api/v1/employees', data).then(res => res.data),

  update: (id: string, data: UpdateEmployeeRequest): Promise<Employee> =>
      apiClient.put(`/employee-service/api/v1/employees/${id}`, data).then(res => res.data),

  delete: (id: string): Promise<void> =>
      apiClient.delete(`/employee-service/api/v1/employees/${id}`).then(() => undefined),
};