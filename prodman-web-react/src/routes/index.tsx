import { createBrowserRouter, Navigate } from 'react-router-dom';
import MainLayout from '../layouts/MainLayout/MainLayout';
import Login from '../pages/Auth/Login';
import Dashboard from '../pages/Dashboard/Dashboard';
import EmployeesList from '../pages/Employees/EmployeesList';
import EmployeeDetail from '../pages/Employees/EmployeeDetail';
import Equipment from '../pages/Equipment/Equipment';
import TestsList from '../pages/Tests/TestsList';
import WorkCalendar from '../pages/Calendar/WorkCalendar';
import WorkstationsList from '../pages/Workstations/WorkstationsList';
import UsersList from '../pages/Users/UsersList';
import PositionsList from '../pages/Dictionaries/Positions/PositionsList';
import DepartmentsList from '../pages/Dictionaries/Departments/DepartmentsList';
import QualificationsList from '../pages/Dictionaries/Qualifications/QualificationsList';
import ShiftPatternsList from '../pages/ShiftPatterns/ShiftPatternsList';
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
          { path: 'employees/:id', element: <EmployeeDetail /> },
          { path: 'equipment', element: <Equipment /> },
          { path: 'tests', element: <TestsList /> },
          { path: 'calendar', element: <WorkCalendar /> },
          { path: 'shift-patterns', element: <ShiftPatternsList /> },
          { path: 'workstations', element: <WorkstationsList /> },
          { path: 'users', element: <UsersList /> },
          // Справочники
          { path: 'dictionaries/positions', element: <PositionsList /> },
          { path: 'dictionaries/departments', element: <DepartmentsList /> },
          { path: 'dictionaries/qualifications', element: <QualificationsList /> },
        ],
      },
    ],
  },
]);