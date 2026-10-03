import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form, Input, Button, Card, Typography, Alert } from 'antd';
import { UserOutlined, LockOutlined, ApartmentOutlined } from '@ant-design/icons';
import { useAuthStore } from '../../store/auth.store';

const { Title, Text } = Typography;

interface LoginFormValues {
  slug?: string;
  username: string;
  password: string;
}

const Login = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const { login } = useAuthStore();

  const onFinish = async (values: LoginFormValues) => {
    setLoading(true);
    setError('');
    try {
      await login({
        slug: values.slug?.trim() || undefined,
        username: values.username.trim(),
        password: values.password,
      });
      navigate('/dashboard');
    } catch (err: any) {
      const msg = err?.response?.data?.message;
      setError(msg || 'Ошибка входа. Проверьте логин, пароль и организацию.');
    } finally {
      setLoading(false);
    }
  };

  return (
      <div
          style={{
            height: '100vh',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            background: 'linear-gradient(to bottom, #1a1a2e, #16213e)',
          }}
      >
        <Card
            style={{
              width: 420,
              background: '#1e1e2f',
              border: '1px solid #2d2d3f',
              borderRadius: 12,
            }}
        >
          <div style={{ textAlign: 'center', marginBottom: 30 }}>
            <Title level={1} style={{ color: '#2E8B57', margin: 0 }}>
              PRODMAN
            </Title>
            <Text style={{ color: '#888' }}>Управление производством</Text>
          </div>

          <Form name="login" onFinish={onFinish} layout="vertical" size="large">
            <Form.Item
                name="slug"
                tooltip="Короткий идентификатор организации. Оставьте пустым, если вы администратор платформы."
            >
              <Input
                  prefix={<ApartmentOutlined style={{ color: '#888' }} />}
                  placeholder="Организация (необязательно)"
                  style={{ background: '#2d2d3f', border: 'none', color: 'white' }}
              />
            </Form.Item>

            <Form.Item
                name="username"
                rules={[{ required: true, message: 'Введите имя пользователя' }]}
            >
              <Input
                  prefix={<UserOutlined style={{ color: '#888' }} />}
                  placeholder="Username"
                  style={{ background: '#2d2d3f', border: 'none', color: 'white' }}
              />
            </Form.Item>

            <Form.Item
                name="password"
                rules={[{ required: true, message: 'Введите пароль' }]}
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
                    style={{ marginBottom: 16, background: '#3d1f1f', border: 'none' }}
                />
            )}

            <Form.Item>
              <Button
                  type="primary"
                  htmlType="submit"
                  block
                  loading={loading}
                  style={{ background: '#0f3460', border: 'none', height: 44 }}
              >
                Войти
              </Button>
            </Form.Item>
          </Form>
        </Card>
      </div>
  );
};

export default Login;