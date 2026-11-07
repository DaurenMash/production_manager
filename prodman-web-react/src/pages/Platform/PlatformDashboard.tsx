import { useEffect, useState } from 'react';
import { Typography, Card, Row, Col, Statistic, Table, Tag, message, Alert } from 'antd';
import { ShopOutlined, CheckCircleOutlined, ClockCircleOutlined, StopOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { tenantsApi } from '../../api/tenants.api';
import type { Tenant } from '../../api/tenants.api';

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

const PlatformDashboard = () => {
    const [tenants, setTenants] = useState<Tenant[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        tenantsApi.getAll(0, 500)
            .then((res) => setTenants(res.content ?? []))
            .catch(() => message.error('Ошибка загрузки тенантов'))
            .finally(() => setLoading(false));
    }, []);

    const total = tenants.length;
    const active = tenants.filter((t) => t.status === 'ACTIVE').length;
    const trial = tenants.filter((t) => t.status === 'TRIAL').length;
    const suspended = tenants.filter((t) => t.status === 'SUSPENDED' || t.status === 'CANCELLED').length;

    const columns = [
        { title: 'Название', dataIndex: 'name', key: 'name' },
        { title: 'Slug', dataIndex: 'slug', key: 'slug', render: (v: string) => <code>{v}</code> },
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
    ];

    return (
        <div>
            <Title level={2} style={{ color: 'white' }}>Дашборд платформы</Title>

            {/* Статистика */}
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

            {/* Таблица тенантов */}
            <Card
                title={<span style={{ color: 'white' }}>Тенанты платформы</span>}
                style={{ background: '#2d2d3f', border: 'none' }}
                styles={{ header: { borderBottom: '1px solid #3d3d4f' } }}
            >
                <Table
                    dataSource={tenants}
                    columns={columns}
                    rowKey="id"
                    loading={loading}
                    pagination={{ pageSize: 20, showSizeChanger: true }}
                />
            </Card>

            {/* Блок заглушек для будущего */}
            <Row gutter={16} style={{ marginTop: 24 }}>
                <Col span={12}>
                    <Card title={<span style={{ color: 'white' }}>Логи действий</span>} style={{ background: '#2d2d3f', border: 'none' }}>
                        <Alert
                            type="info"
                            showIcon
                            message="Раздел в разработке. Здесь будет аудит действий пользователей платформы (создание тенантов, смена планов, входы и т.д.)."
                            style={{ background: '#1e1e2f', border: '1px solid #3d3d4f' }}
                        />
                    </Card>
                </Col>
                <Col span={12}>
                    <Card title={<span style={{ color: 'white' }}>Метрики платформы</span>} style={{ background: '#2d2d3f', border: 'none' }}>
                        <Alert
                            type="info"
                            showIcon
                            message="Раздел в разработке. Здесь будет статистика: активные пользователи за сутки, нагрузка на сервисы, ошибки, время работы."
                            style={{ background: '#1e1e2f', border: '1px solid #3d3d4f' }}
                        />
                    </Card>
                </Col>
            </Row>
        </div>
    );
};

export default PlatformDashboard;