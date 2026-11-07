import { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { Layout, Menu, Button, Dropdown, Avatar, Space, Badge, Tag } from 'antd';
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
} from '@ant-design/icons';
import { useAuthStore } from '../../store/auth.store';

const { Header, Sider, Content } = Layout;

const MainLayout = () => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuthStore();

  const currentRole = user?.role;

  // Флаги для удобства
  const isAdmin = currentRole === 'ADMIN';
  const isTenantVisitor = currentRole === 'TENANT_VISITOR';
  const isHead = currentRole === 'HEAD_OF_DEPARTMENT';
  const isDeptVisitor = currentRole === 'DEPT_VISITOR';

  // База: то, что видят все (Календарь и Отчёты)
  let menuItems: any[] = [
    { key: '/calendar', icon: <CalendarOutlined />, label: 'Календарь' },
    { key: '/reports', icon: <BarChartOutlined />, label: 'Отчёты' },
  ];

  // То, что видят Админ и Визитор тенанта (всё, кроме управления пользователями для визитора)
  if (isAdmin || isTenantVisitor) {
    menuItems = [
      { key: '/dashboard', icon: <DashboardOutlined />, label: 'Дашборд' },
      { key: '/employees', icon: <TeamOutlined />, label: 'Работники' },
      { key: '/equipment', icon: <AppstoreOutlined />, label: 'Оборудование' },
      { key: '/tests', icon: <ExperimentOutlined />, label: 'Тестирование' },
      { key: '/calendar', icon: <CalendarOutlined />, label: 'Календарь' },
      { key: '/reports', icon: <BarChartOutlined />, label: 'Отчёты' },
      { key: '/shift-patterns', icon: <ClockCircleOutlined />, label: 'Шаблоны смен' },
      { key: '/workstations', icon: <SettingOutlined />, label: 'Рабочие места' },
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

    // Настройки тенанта и Пользователи доступны только админу тенанта
    if (isAdmin) {
      menuItems.push({ key: '/settings', icon: <SettingOutlined />, label: 'Настройки' });
      menuItems.push({ key: '/users', icon: <UserOutlined />, label: 'Пользователи' });
    }
  }

  // Глава департамента (Head of Department)
  if (isHead) {
    menuItems = [
      { key: '/employees', icon: <TeamOutlined />, label: 'Мои сотрудники' },
      { key: '/calendar', icon: <CalendarOutlined />, label: 'Календарь' },
      { key: '/reports', icon: <BarChartOutlined />, label: 'Отчёты' },
      { key: '/users', icon: <UserOutlined />, label: 'Пользователи' }, // Только своего отдела
    ];
  }

  // Визитор департамента (DEPT_VISITOR)
  if (isDeptVisitor) {
    menuItems = [
      { key: '/calendar', icon: <CalendarOutlined />, label: 'Календарь' },
      { key: '/reports', icon: <BarChartOutlined />, label: 'Отчёты' },
    ];
  }

  // Платформенный админ видит только управление тенантами (пока пусто)
  if (currentRole === 'PLATFORM_ADMIN') {
    menuItems = [
      { key: '/dashboard', icon: <DashboardOutlined />, label: 'Тенанты платформы' },
    ];
  }

  const openKeys = location.pathname.startsWith('/dictionaries') ? ['dictionaries'] : [];

  const handleLogout = () => {
    logout();
    navigate('/login');
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
            <Button
                type="text"
                icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                onClick={() => setCollapsed(!collapsed)}
                style={{ fontSize: 16, width: 64, height: 64, color: 'white' }}
            />

            <Space>
              <Badge dot>
                <Avatar style={{ background: '#2E8B57' }}>
                  {user?.username?.[0]?.toUpperCase() || 'U'}
                </Avatar>
              </Badge>
              <Dropdown menu={{
                items: [{ key: 'logout', icon: <LogoutOutlined />, label: 'Выйти', onClick: handleLogout }],
              }} placement="bottomRight">
                <Space style={{ color: 'white', cursor: 'pointer' }}>
                  <span>{user?.username}</span>
                  <Tag color="blue">{currentRole}</Tag>
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