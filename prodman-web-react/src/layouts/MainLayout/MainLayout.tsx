import { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { Layout, Menu, Button, Dropdown, Avatar, Space, Badge, Tag, message } from 'antd';
import {
  DashboardOutlined,
  TeamOutlined,
  AppstoreOutlined,
  ExperimentOutlined,
  CalendarOutlined,
  SettingOutlined,
  UserOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  SafetyCertificateOutlined,
  BookOutlined,
  ClockCircleOutlined,
  BarChartOutlined,
  IdcardOutlined,
  ApartmentOutlined,
  ArrowLeftOutlined,
  FileTextOutlined,
  LineChartOutlined,
} from '@ant-design/icons';
import { useAuthStore } from '../../store/auth.store';

const { Header, Sider, Content } = Layout;

const MainLayout = () => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout, isImpersonating, exitImpersonation } = useAuthStore();

  const currentRole = user?.role;

  // Роли
  const isPlatformAdmin = currentRole === 'PLATFORM_ADMIN';
  const isAdmin = currentRole === 'ADMIN';
  const isTenantVisitor = currentRole === 'TENANT_VISITOR';
  const isHead = currentRole === 'HEAD_OF_DEPARTMENT';
  const isDeptVisitor = currentRole === 'DEPT_VISITOR';

  // Базовое меню тенанта (Календарь + Отчёты) — для всех тенантных ролей
  let menuItems: any[] = [
    { key: '/calendar', icon: <CalendarOutlined />, label: 'Календарь' },
    { key: '/reports', icon: <BarChartOutlined />, label: 'Отчёты' },
  ];

  // ADMIN / TENANT_VISITOR — расширенное меню
  if (isAdmin || isTenantVisitor) {
    menuItems = [
      { key: '/dashboard', icon: <DashboardOutlined />, label: 'Дашборд' },
      { key: '/employees', icon: <TeamOutlined />, label: 'Работники' },
      { key: '/equipment', icon: <AppstoreOutlined />, label: 'Оборудование' },
      { key: '/tests', icon: <ExperimentOutlined />, label: 'Тестирование' },
      { key: '/calendar', icon: <CalendarOutlined />, label: 'Календарь' },
      { key: '/reports', icon: <BarChartOutlined />, label: 'Отчёты' },
      { key: '/shift-patterns', icon: <ClockCircleOutlined />, label: 'Шаблоны смен' },
      { key: '/workstations', icon: <SettingOutlined />, label: 'Рабочие станции' },
      {
        key: 'dictionaries',
        icon: <BookOutlined />,
        label: 'Справочники',
        children: [
          { key: '/dictionaries/positions', icon: <IdcardOutlined />, label: 'Должности' },
          { key: '/dictionaries/departments', icon: <ApartmentOutlined />, label: 'Отделы' },
          { key: '/dictionaries/qualifications', icon: <SafetyCertificateOutlined />, label: 'Квалификации' },
        ],
      },
    ];

    if (isAdmin) {
      menuItems.push({ key: '/settings', icon: <SettingOutlined />, label: 'Настройки' });
      menuItems.push({ key: '/users', icon: <UserOutlined />, label: 'Пользователи' });
    }
  }

  // HEAD_OF_DEPARTMENT — только свой отдел
  if (isHead) {
    menuItems = [
      { key: '/employees', icon: <TeamOutlined />, label: 'Мои сотрудники' },
      { key: '/calendar', icon: <CalendarOutlined />, label: 'Календарь' },
      { key: '/reports', icon: <BarChartOutlined />, label: 'Отчёты' },
      { key: '/users', icon: <UserOutlined />, label: 'Пользователи' },
    ];
  }

  // DEPT_VISITOR — минимальный набор
  if (isDeptVisitor) {
    menuItems = [
      { key: '/calendar', icon: <CalendarOutlined />, label: 'Календарь' },
      { key: '/reports', icon: <BarChartOutlined />, label: 'Отчёты' },
    ];
  }

  // VISITOR — демо-доступ: только календарь и отчёты (read-only на бэкенде)
  if (currentRole === 'VISITOR') {
    menuItems = [
      { key: '/calendar', icon: <CalendarOutlined />, label: 'Календарь (демо)' },
      { key: '/reports', icon: <BarChartOutlined />, label: 'Отчёты (демо)' },
      { key: '/demo', icon: <ExperimentOutlined />, label: 'О платформе' },
    ];
  }

  // PLATFORM_ADMIN — только дашборд платформы (но при имперсонации видит меню тенанта)
  if (isPlatformAdmin) {
    menuItems = [
      { key: '/platform-dashboard', icon: <DashboardOutlined />, label: 'Дашборд платформы' },
      { key: '/platform-logs', icon: <FileTextOutlined />, label: 'Логи действий' },
      { key: '/platform-metrics', icon: <LineChartOutlined />, label: 'Метрики' },
    ];
  }

  const openKeys = location.pathname.startsWith('/dictionaries') ? ['dictionaries'] : [];

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleExitImpersonation = () => {
    exitImpersonation();
    message.success('Выход из тенанта');
    navigate('/platform-dashboard');
  };

  return (
      <Layout style={{ minHeight: '100vh' }}>
        <Sider
            trigger={null}
            collapsible
            collapsed={collapsed}
            style={{ background: '#1e1e2f', borderRight: '1px solid #2d2d3f' }}
        >
          <div style={{ height: 64, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#2E8B57', fontSize: collapsed ? 20 : 28, fontWeight: 'bold' }}>
            {collapsed ? 'P' : 'PRODMAN'}
          </div>
          <Menu
              theme="dark"
              mode="inline"
              selectedKeys={[location.pathname]}
              defaultOpenKeys={openKeys}
              items={menuItems}
              onClick={({ key }) => navigate(key)}
              style={{ background: 'transparent', borderRight: 'none' }}
          />
        </Sider>

        <Layout style={{ background: '#1e1e2f' }}>
          <Header style={{ background: '#2d2d3f', padding: '0 24px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid #3d3d4f' }}>
            <Space>
              <Button
                  type="text"
                  icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                  onClick={() => setCollapsed(!collapsed)}
                  style={{ fontSize: 16, width: 64, height: 64, color: 'white' }}
              />
              {isImpersonating && (
                  <Tag color="orange" style={{ marginLeft: 8 }}>
                    Имперсонация: {user?.tenantId?.slice(0, 8)}
                  </Tag>
              )}
            </Space>

            <Space>
              {isImpersonating && (
                  <Button
                      icon={<ArrowLeftOutlined />}
                      onClick={handleExitImpersonation}
                      type="primary"
                      danger
                      size="small"
                  >
                    Выйти из тенанта
                  </Button>
              )}
              <Badge dot>
                <Avatar style={{ background: '#2E8B57' }}>
                  {user?.username?.[0]?.toUpperCase() || 'U'}
                </Avatar>
              </Badge>
              <Dropdown menu={{
                items: [
                  { key: 'logout', icon: <LogoutOutlined />, label: isImpersonating ? 'Выйти полностью' : 'Выйти', onClick: handleLogout },
                ],
              }} placement="bottomRight">
                <Space style={{ color: 'white', cursor: 'pointer' }}>
                  <span>{user?.username}</span>
                  <Tag color={isImpersonating ? 'orange' : 'blue'}>
                    {isImpersonating ? 'ADMIN (imp.)' : currentRole}
                  </Tag>
                </Space>
              </Dropdown>
            </Space>
          </Header>

          <Content style={{ margin: '16px', padding: '24px', background: '#1e1e2f', borderRadius: 8, minHeight: 280, overflow: 'auto' }}>
            <Outlet />
          </Content>
        </Layout>
      </Layout>
  );
};

export default MainLayout;