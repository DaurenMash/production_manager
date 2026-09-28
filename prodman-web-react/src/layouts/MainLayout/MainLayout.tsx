import { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { Layout, Menu, Button, Dropdown, Avatar, Space, Badge } from 'antd';
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
} from '@ant-design/icons';
import { useAuthStore } from '../../store/auth.store';

const { Header, Sider, Content } = Layout;

const MainLayout = () => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuthStore();

  const menuItems = [
    { key: '/dashboard', icon: <DashboardOutlined />, label: 'Дашборд' },
    { key: '/employees', icon: <TeamOutlined />, label: 'Работники' },
    { key: '/equipment', icon: <AppstoreOutlined />, label: 'Оборудование' },
    { key: '/tests', icon: <ExperimentOutlined />, label: 'Тестирование' },
    { key: '/calendar', icon: <CalendarOutlined />, label: 'Календарь' },
    { key: '/workstations', icon: <SettingOutlined />, label: 'Рабочие места' },
  ];

  if (user?.role === 'ADMIN') {
    menuItems.push({ key: '/users', icon: <UserOutlined />, label: 'Пользователи' });
  }

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
        style={{
          background: '#1e1e2f',
          borderRight: '1px solid #2d2d3f',
        }}
      >
        <div style={{ 
          height: 64, 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'center',
          color: '#2E8B57',
          fontSize: collapsed ? 20 : 28,
          fontWeight: 'bold',
        }}>
          {collapsed ? 'P' : 'PRODMAN'}
        </div>
        
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
          style={{ background: 'transparent', borderRight: 'none' }}
        />
      </Sider>

      <Layout style={{ background: '#1e1e2f' }}>
        <Header style={{
          background: '#2d2d3f',
          padding: '0 24px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          borderBottom: '1px solid #3d3d4f',
        }}>
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
              items: [
                { key: 'logout', icon: <LogoutOutlined />, label: 'Выйти', onClick: handleLogout },
              ],
            }} placement="bottomRight">
              <Space style={{ color: 'white', cursor: 'pointer' }}>
                <span>{user?.username}</span>
                <span style={{ fontSize: 12, color: '#888' }}>{user?.role}</span>
              </Space>
            </Dropdown>
          </Space>
        </Header>

        <Content style={{
          margin: '16px',
          padding: '24px',
          background: '#1e1e2f',
          borderRadius: 8,
          minHeight: 280,
          overflow: 'auto',
        }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;
