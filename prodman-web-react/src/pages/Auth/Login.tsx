import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form, Input, Button, Card, Typography, Alert } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useAuthStore } from '../../store/auth.store';

const { Title, Text } = Typography;

const Login = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const { login, isAuthenticated } = useAuthStore();

  useEffect(() => {
    if (isAuthenticated) {
      navigate('/dashboard', { replace: true });
    }
  }, [isAuthenticated, navigate]);

  const onFinish = async (values: { username: string; password: string }) => {
    setLoading(true);
    setError('');

    try {
      await login(values.username.trim(), values.password);
      navigate('/dashboard', { replace: true });
    } catch (err: any) {
      const serverMessage =
          err.response?.data?.message ||
          err.response?.data?.error ||
          err.message;

      if (err.response?.status === 401) {
        setError('Неверный логин или пароль');
      } else if (err.response?.status === 500) {
        setError('Ошибка сервера. Попробуйте позже');
      } else if (!err.response) {
        setError('Нет соединения с сервером');
      } else {
        setError(serverMessage || 'Ошибка входа');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
      <div style={{
        height: '100vh',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        background: 'linear-gradient(to bottom, #1a1a2e, #16213e)',
      }}>
        <Card
            style={{
              width: 400,
              background: '#1e1e2f',
              border: '1px solid #2d2d3f',
              borderRadius: 12,
              boxShadow: '0 8px 32px rgba(0, 0, 0, 0.3)',
            }}
        >
          <div style={{ textAlign: 'center', marginBottom: 30 }}>
            <Title level={1} style={{ color: '#2E8B57', margin: 0 }}>PRODMAN</Title>
            <Text style={{ color: '#888' }}>Управление производством</Text>
          </div>

          <Form
              name="login"
              onFinish={onFinish}
              layout="vertical"
              size="large"
              autoComplete="off"
          >
            <Form.Item
                name="username"
                rules={[
                  { required: true, message: 'Введите имя пользователя' },
                  { min: 3, message: 'Минимум 3 символа' },
                ]}
            >
              <Input
                  prefix={<UserOutlined style={{ color: '#888' }} />}
                  placeholder="Username"
                  autoFocus
                  style={{ background: '#2d2d3f', border: 'none', color: 'white' }}
              />
            </Form.Item>

            <Form.Item
                name="password"
                rules={[
                  { required: true, message: 'Введите пароль' },
                  { min: 3, message: 'Минимум 3 символа' },
                ]}
            >
              <Input.Password
                  prefix={<LockOutlined style={{ color: '#888' }} />}
                  placeholder="Пароль"
                  style={{ background: '#2d2d3f', border: 'none', color: 'white' }}
              />
            </Form.Item>

            {error && (
                <Alert
                    message={error}
                    type="error"
                    showIcon
                    closable
                    onClose={() => setError('')}
                    style={{ marginBottom: 16, background: '#3d1f1f', border: 'none' }}
                />
            )}

            <Form.Item style={{ marginBottom: 8 }}>
              <Button
                  type="primary"
                  htmlType="submit"
                  block
                  loading={loading}
                  style={{ background: '#0f3460', border: 'none', height: 44, fontWeight: 600 }}
              >
                Войти
              </Button>
            </Form.Item>
          </Form>

          <div style={{ textAlign: 'center', marginTop: 16 }}>
            <Text style={{ color: '#666', fontSize: 12 }}>
              Dev-доступ: <Text code style={{ background: '#2d2d3f', color: '#2E8B57' }}>admin / admin</Text>
            </Text>
          </div>
        </Card>
      </div>
  );
};

export default Login;