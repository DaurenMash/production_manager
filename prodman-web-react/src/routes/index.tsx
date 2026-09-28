import { createBrowserRouter, Navigate } from 'react-router-dom';
import MainLayout from '../layouts/MainLayout/MainLayout';
import Login from '../pages/Auth/Login';
import Dashboard from '../pages/Dashboard/Dashboard';
import EmployeesList from '../pages/Employees/EmployeesList';
import Equipment from '../pages/Equipment/Equipment';
import TestsList from '../pages/Tests/TestsList';
import WorkCalendar from '../pages/Calendar/WorkCalendar';
import WorkstationsList from '../pages/Workstations/WorkstationsList';
import UsersList from '../pages/Users/UsersList';
import { ProtectedRoute } from '../components/ProtectedRoute';

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <Login />,
  },
  {
    path: '/',
    element: <ProtectedRoute />,
    children: [
      {
        element: <MainLayout />,
        children: [
          { index: true, element: <Navigate to="/dashboard" replace /> },
          { path: 'dashboard', element: <Dashboard /> },
          { path: 'employees', element: <EmployeesList /> },
          { path: 'equipment', element: <Equipment /> },
          { path: 'tests', element: <TestsList /> },
          { path: 'calendar', element: <WorkCalendar /> },
          { path: 'workstations', element: <WorkstationsList /> },
          { path: 'users', element: <UsersList /> },
        ],
      },
    ],
  },
]);
