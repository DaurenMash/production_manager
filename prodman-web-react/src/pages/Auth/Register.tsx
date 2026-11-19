import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Form, Input, Button, Card, Typography, Alert, message } from 'antd';
import {
    ApartmentOutlined,
    UserOutlined,
    MailOutlined,
    LockOutlined,
    TagOutlined,
} from '@ant-design/icons';
import { authApi } from '../../api/auth.api';
import { useAuthStore } from '../../store/auth.store';

const { Title, Text } = Typography;

interface RegisterFormValues {
    tenantName: string;
    tenantSlug: string;
    username: string;
    email: string;
    password: string;
}

const Register = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const onFinish = async (values: RegisterFormValues) => {
        setLoading(true);
        setError('');
        try {
            const res = await authApi.registerTenant({
                tenantName: values.tenantName.trim(),
                tenantSlug: values.tenantSlug.trim(),
                username: values.username.trim(),
                email: values.email.trim(),
                password: values.password,
            });

            localStorage.setItem('accessToken', res.accessToken);
            localStorage.setItem('refreshToken', res.refreshToken);
            localStorage.setItem('role', res.role);
            if (res.tenantId) {
                localStorage.setItem('tenantId', res.tenantId);
            }

            useAuthStore.setState({
                user: {
                    id: res.userId,
                    tenantId: res.tenantId,
                    username: res.username,
                    email: res.email,
                    role: res.role,
                    departmentId: res.departmentId ?? null,
                },
                accessToken: res.accessToken,
                refreshToken: res.refreshToken,
                tenantId: res.tenantId,
                tenantSlug: res.tenantSlug,
                tenantStatus: res.tenantStatus,
                tenantPlan: res.tenantPlan,
                isAuthenticated: true,
                isImpersonating: false,
            });

            message.success('Организация создана. Добро пожаловать!');
            navigate('/dashboard');
        } catch (err: any) {
            const msg = err?.response?.data?.message;
            setError(msg || 'Ошибка регистрации. Проверьте данные и попробуйте снова.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div
            style={{
                minHeight: '100vh',
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                background: 'linear-gradient(to bottom, #1a1a2e, #16213e)',
                padding: '24px 0',
            }}
        >
            <Card
                style={{
                    width: 480,
                    background: '#1e1e2f',
                    border: '1px solid #2d2d3f',
                    borderRadius: 12,
                }}
            >
                <div style={{ textAlign: 'center', marginBottom: 24 }}>
                    <Title level={2} style={{ color: '#2E8B57', margin: 0 }}>
                        Регистрация организации
                    </Title>
                    <Text style={{ color: '#888' }}>
                        Создайте аккаунт и получите 30 дней бесплатно
                    </Text>
                </div>

                <Form
                    name="register"
                    onFinish={onFinish}
                    layout="vertical"
                    size="large"
                    requiredMark={false}
                >
                    <Form.Item
                        name="tenantName"
                        label={<span style={{ color: '#ccc' }}>Название организации</span>}
                        rules={[
                            { required: true, message: 'Введите название организации' },
                            { min: 2, max: 255, message: 'От 2 до 255 символов' },
                        ]}
                    >
                        <Input
                            prefix={<ApartmentOutlined style={{ color: '#888' }} />}
                            placeholder="ООО «Ромашка»"
                            style={{ background: '#2d2d3f', border: 'none', color: 'white' }}
                        />
                    </Form.Item>

                    <Form.Item
                        name="tenantSlug"
                        label={<span style={{ color: '#ccc' }}>Короткий идентификатор (slug)</span>}
                        tooltip="Только латиница (любой регистр), цифры, дефис (-) и нижний дефис (_)."
                        rules={[
                            { required: true, message: 'Введите slug' },
                            {
                                pattern: /^[a-zA-Z0-9_-]+$/,
                                message: 'Только латиница, цифры, дефис (-) и нижний дефис (_)',
                            },
                            { min: 2, max: 64, message: 'От 2 до 64 символов' },
                        ]}
                    >
                        <Input
                            prefix={<TagOutlined style={{ color: '#888' }} />}
                            placeholder="romashka"
                            style={{ background: '#2d2d3f', border: 'none', color: 'white' }}
                        />
                    </Form.Item>

                    <Form.Item
                        name="username"
                        label={<span style={{ color: '#ccc' }}>Имя пользователя (админ)</span>}
                        rules={[
                            { required: true, message: 'Введите имя пользователя' },
                            { min: 3, max: 64, message: 'От 3 до 64 символов' },
                        ]}
                    >
                        <Input
                            prefix={<UserOutlined style={{ color: '#888' }} />}
                            placeholder="admin"
                            style={{ background: '#2d2d3f', border: 'none', color: 'white' }}
                        />
                    </Form.Item>

                    <Form.Item
                        name="email"
                        label={<span style={{ color: '#ccc' }}>Email</span>}
                        rules={[
                            { required: true, message: 'Введите email' },
                            { type: 'email', message: 'Некорректный email' },
                        ]}
                    >
                        <Input
                            prefix={<MailOutlined style={{ color: '#888' }} />}
                            placeholder="admin@company.ru"
                            style={{ background: '#2d2d3f', border: 'none', color: 'white' }}
                        />
                    </Form.Item>

                    <Form.Item
                        name="password"
                        label={<span style={{ color: '#ccc' }}>Пароль</span>}
                        rules={[
                            { required: true, message: 'Введите пароль' },
                            { min: 6, max: 100, message: 'От 6 до 100 символов' },
                        ]}
                    >
                        <Input.Password
                            prefix={<LockOutlined style={{ color: '#888' }} />}
                            placeholder="Минимум 6 символов"
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
                            style={{ background: '#2E8B57', border: 'none', height: 44 }}
                        >
                            Создать организацию
                        </Button>
                    </Form.Item>

                    <div style={{ textAlign: 'center', color: '#888', fontSize: 13 }}>
                        Уже есть аккаунт?{' '}
                        <Link to="/login" style={{ color: '#2E8B57' }}>
                            Войти
                        </Link>
                    </div>
                </Form>
            </Card>
        </div>
    );
};

export default Register;