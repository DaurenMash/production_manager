import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Typography, Card, Row, Col, Statistic, Table, Tag, message,
    Button, Space, Popconfirm, Modal, Form, Input, Alert,
} from 'antd';
import {
    ShopOutlined, CheckCircleOutlined, ClockCircleOutlined, StopOutlined, LoginOutlined,
    PlusOutlined, ApartmentOutlined, TagOutlined, UserOutlined, MailOutlined, LockOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import { tenantsApi } from '../../api/tenants.api';
import type { Tenant } from '../../api/tenants.api';
import { authApi } from '../../api/auth.api';
import { useAuthStore } from '../../store/auth.store';

const { Title } = Typography;

const STATUS_COLORS: Record<string, string> = {
    TRIAL: 'orange',
    ACTIVE: 'green',
    SUSPENDED: 'red',
    CANCELLED: 'default',
};

const PLAN_COLORS: Record<string, string> = {
    FREE: 'blue',
    BASIC: 'purple',
    UNLIM: 'gold',
};

interface CreateTenantFormValues {
    tenantName: string;
    tenantSlug: string;
    username: string;
    email: string;
    password: string;
}

const PlatformDashboard = () => {
    const [tenants, setTenants] = useState<Tenant[]>([]);
    const [loading, setLoading] = useState(true);
    const [impersonating, setImpersonating] = useState<string | null>(null);

    const [createOpen, setCreateOpen] = useState(false);
    const [creating, setCreating] = useState(false);
    const [createForm] = Form.useForm<CreateTenantFormValues>();

    const navigate = useNavigate();
    const { impersonate } = useAuthStore();

    const load = async () => {
        setLoading(true);
        try {
            const res = await tenantsApi.getAll(0, 500);
            setTenants(res.content ?? []);
        } catch {
            message.error('Ошибка загрузки тенантов');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        load();
    }, []);

    const handleImpersonate = async (tenant: Tenant) => {
        setImpersonating(tenant.id);
        try {
            await impersonate(tenant.id);
            message.success(`Вход в тенант «${tenant.name}»`);
            navigate('/dashboard');
        } catch (err: any) {
            message.error(err.response?.data?.message || 'Ошибка входа в тенант');
        } finally {
            setImpersonating(null);
        }
    };

    const handleCreateTenant = async () => {
        try {
            const values = await createForm.validateFields();
            setCreating(true);
            await authApi.registerTenant({
                tenantName: values.tenantName.trim(),
                tenantSlug: values.tenantSlug.trim(),
                username: values.username.trim(),
                email: values.email.trim(),
                password: values.password,
            });
            message.success('Тенант создан');
            setCreateOpen(false);
            createForm.resetFields();
            load();
        } catch (err: any) {
            if (err.errorFields) return;
            message.error(err.response?.data?.message || 'Ошибка создания тенанта');
        } finally {
            setCreating(false);
        }
    };

    const total = tenants.length;
    const active = tenants.filter((t) => t.status === 'ACTIVE').length;
    const trial = tenants.filter((t) => t.status === 'TRIAL').length;
    const suspended = tenants.filter((t) => t.status === 'SUSPENDED' || t.status === 'CANCELLED').length;

    const columns = [
        { title: 'Название', dataIndex: 'name', key: 'name' },
        { title: 'Slug', dataIndex: 'slug', key: 'slug', render: (v: string) => <code>{v}</code> },
        {
            title: 'ID тенанта',
            dataIndex: 'id',
            key: 'id',
            width: 280,
            render: (v: string) => (
                <code
                    style={{ fontSize: 11, cursor: 'pointer', color: '#8ecf9e' }}
                    onClick={() => {
                        navigator.clipboard.writeText(v);
                        message.success('ID скопирован');
                    }}
                    title="Кликните, чтобы скопировать"
                >
                    {v}
                </code>
            ),
        },
        {
            title: 'Статус',
            dataIndex: 'status',
            key: 'status',
            render: (v: string) => <Tag color={STATUS_COLORS[v]}>{v}</Tag>,
        },
        {
            title: 'План',
            dataIndex: 'plan',
            key: 'plan',
            render: (v: string) => <Tag color={PLAN_COLORS[v]}>{v}</Tag>,
        },
        {
            title: 'Создан',
            dataIndex: 'createdAt',
            key: 'createdAt',
            render: (v: string) => dayjs(v).format('DD.MM.YYYY HH:mm'),
        },
        {
            title: 'Действия',
            key: 'actions',
            render: (_: unknown, t: Tenant) => (
                <Space>
                    <Popconfirm
                        title={`Войти в тенант «${t.name}»?`}
                        description="Вы получите права ADMIN тенанта. Аудит действий будет вестись от вашего имени (PLATFORM_ADMIN)."
                        onConfirm={() => handleImpersonate(t)}
                        okText="Войти"
                        cancelText="Отмена"
                    >
                        <Button
                            icon={<LoginOutlined />}
                            type="primary"
                            size="small"
                            loading={impersonating === t.id}
                            style={{ background: '#2E8B57', borderColor: '#2E8B57' }}
                        >
                            Войти в тенант
                        </Button>
                    </Popconfirm>
                </Space>
            ),
        },
    ];

    return (
        <div>
            <Title level={2} style={{ color: 'white' }}>Дашборд платформы</Title>

            <Row gutter={16} style={{ marginBottom: 24 }}>
                <Col span={6}>
                    <Card style={{ background: '#2d2d3f', border: 'none' }}>
                        <Statistic title="Всего тенантов" value={total} prefix={<ShopOutlined />} valueStyle={{ color: '#2E8B57' }} />
                    </Card>
                </Col>
                <Col span={6}>
                    <Card style={{ background: '#2d2d3f', border: 'none' }}>
                        <Statistic title="Активные" value={active} prefix={<CheckCircleOutlined />} valueStyle={{ color: '#2ecc71' }} />
                    </Card>
                </Col>
                <Col span={6}>
                    <Card style={{ background: '#2d2d3f', border: 'none' }}>
                        <Statistic title="На триале" value={trial} prefix={<ClockCircleOutlined />} valueStyle={{ color: '#e67e22' }} />
                    </Card>
                </Col>
                <Col span={6}>
                    <Card style={{ background: '#2d2d3f', border: 'none' }}>
                        <Statistic title="Заблокированы" value={suspended} prefix={<StopOutlined />} valueStyle={{ color: '#e74c3c' }} />
                    </Card>
                </Col>
            </Row>

            <Card
                title={<span style={{ color: 'white' }}>Тенанты платформы</span>}
                style={{ background: '#2d2d3f', border: 'none' }}
                styles={{ header: { borderBottom: '1px solid #3d3d4f' } }}
                extra={
                    <Button
                        type="primary"
                        icon={<PlusOutlined />}
                        onClick={() => {
                            createForm.resetFields();
                            setCreateOpen(true);
                        }}
                        style={{ background: '#2E8B57', borderColor: '#2E8B57' }}
                    >
                        Создать тенант
                    </Button>
                }
            >
                <Table
                    dataSource={tenants}
                    columns={columns}
                    rowKey="id"
                    loading={loading}
                    pagination={{ pageSize: 20, showSizeChanger: true }}
                />
            </Card>

            <Modal
                title="Создать тенант"
                open={createOpen}
                onOk={handleCreateTenant}
                onCancel={() => {
                    setCreateOpen(false);
                    createForm.resetFields();
                }}
                okText="Создать"
                cancelText="Отмена"
                confirmLoading={creating}
                width={520}
            >
                <Alert
                    type="info"
                    showIcon
                    style={{ marginBottom: 16, background: '#1e1e2f', border: '1px solid #3d3d4f' }}
                    message="Тенант создаётся со статусом TRIAL на 30 дней. Админ тенанта получит доступ к своему кабинету."
                />
                <Form form={createForm} layout="vertical" requiredMark={false}>
                    <Form.Item
                        name="tenantName"
                        label="Название организации"
                        rules={[
                            { required: true, message: 'Введите название' },
                            { min: 2, max: 255, message: 'От 2 до 255 символов' },
                        ]}
                    >
                        <Input
                            prefix={<ApartmentOutlined style={{ color: '#888' }} />}
                            placeholder="ООО «Ромашка»"
                        />
                    </Form.Item>

                    <Form.Item
                        name="tenantSlug"
                        label="Slug (короткий идентификатор)"
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
                        />
                    </Form.Item>

                    <Form.Item
                        name="username"
                        label="Имя пользователя (админ)"
                        rules={[
                            { required: true, message: 'Введите имя пользователя' },
                            { min: 3, max: 64, message: 'От 3 до 64 символов' },
                        ]}
                    >
                        <Input
                            prefix={<UserOutlined style={{ color: '#888' }} />}
                            placeholder="admin"
                        />
                    </Form.Item>

                    <Form.Item
                        name="email"
                        label="Email"
                        rules={[
                            { required: true, message: 'Введите email' },
                            { type: 'email', message: 'Некорректный email' },
                        ]}
                    >
                        <Input
                            prefix={<MailOutlined style={{ color: '#888' }} />}
                            placeholder="admin@company.ru"
                        />
                    </Form.Item>

                    <Form.Item
                        name="password"
                        label="Пароль"
                        rules={[
                            { required: true, message: 'Введите пароль' },
                            { min: 6, max: 100, message: 'От 6 до 100 символов' },
                        ]}
                    >
                        <Input.Password
                            prefix={<LockOutlined style={{ color: '#888' }} />}
                            placeholder="Минимум 6 символов"
                        />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default PlatformDashboard;