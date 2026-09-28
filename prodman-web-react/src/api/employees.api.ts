import apiClient from './client';

export interface Employee {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  department: string;
  position: string | null;
  status: string;
  userId: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateEmployeeRequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
  department?: string;
  position?: string;
  status?: string;
}

export type UpdateEmployeeRequest = Partial<CreateEmployeeRequest>;

export const employeesApi = {
  getAll: (): Promise<Employee[]> =>
      apiClient.get('/employee-service/api/v1/employees').then(res => res.data),

  getById: (id: string): Promise<Employee> =>
      apiClient.get(`/employee-service/api/v1/employees/${id}`).then(res => res.data),

  getByEmail: (email: string): Promise<Employee> =>
      apiClient.get(`/employee-service/api/v1/employees/email/${email}`).then(res => res.data),

  getByStatus: (status: string): Promise<Employee[]> =>
      apiClient.get(`/employee-service/api/v1/employees/status/${status}`).then(res => res.data),

  create: (data: CreateEmployeeRequest): Promise<Employee> =>
      apiClient.post('/employee-service/api/v1/employees', data).then(res => res.data),

  update: (id: string, data: UpdateEmployeeRequest): Promise<Employee> =>
      apiClient.put(`/employee-service/api/v1/employees/${id}`, data).then(res => res.data),

  delete: (id: string): Promise<void> =>
      apiClient.delete(`/employee-service/api/v1/employees/${id}`).then(() => undefined),
};