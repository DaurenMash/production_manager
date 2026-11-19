import { useEffect, useState } from 'react';
import {
    Typography, Card, Table, Tag, Select, DatePicker, Space, Button, Alert, message,
} from 'antd';
import { ReloadOutlined } from '@ant-design/icons';
import dayjs, { Dayjs } from 'dayjs';
import apiClient from '../../api/client';
import { tenantsApi } from '../../api/tenants.api';
import type { Tenant } from '../../api/tenants.api';

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;

interface AuditEvent {
    id: string;
    tenantId: string | null;
    userId: string | null;
    username: string | null;
    action: string;
    target: string | null;
    ip: string | null;
    metadata: string | null;
    createdAt: string;
}

interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

const ACTION_COLORS: Record<string, string> = {
    LOGIN: 'blue',
    IMPERSONATE: 'orange',
    CREATE_USER: 'green',
    DELETE_USER: 'red',
    ASSIGN_TEMP_ROLE: 'purple',
    REVOKE_TEMP_ROLE: 'magenta',
    CREATE_TENANT: 'cyan',
};

const ALL_ACTIONS = Object.keys(ACTION_COLORS);

const PlatformLogs = () => {
    const [logs, setLogs] = useState<AuditEvent[]>([]);
    const [total, setTotal] = useState(0);
    const [page, setPage] = useState(1);
    const [pageSize, setPageSize] = useState(50);
    const [tenants, setTenants] = useState<Tenant[]>([]);
    const [loading, setLoading] = useState(false);
    const [tenantFilter, setTenantFilter] = useState<string | undefined>();
    const [actionFilter, setActionFilter] = useState<string | undefined>();
    const [range, setRange] = useState<[Dayjs, Dayjs]>([
        dayjs().subtract(7, 'day'),
        dayjs(),
    ]);

    const loadTenants = async () => {
        try {
            const res = await tenantsApi.getAll(0, 500);
            setTenants(res.content ?? []);
        } catch {
            // silent
        }
    };

    const loadLogs = async () => {
        setLoading(true);
        try {
            const params: Record<string, string | number> = {
                page: page - 1,
                size: pageSize,
            };
            if (tenantFilter) params.tenantId = tenantFilter;
            if (actionFilter) params.action = actionFilter;
            if (range[0]) params.from = range[0].startOf('day').toISOString();
            if (range[1]) params.to = range[1].endOf('day').toISOString();

            const res = await apiClient
                .get<PageResponse<AuditEvent>>('/user-service/api/v1/platform/audit', { params })
                .then((r) => r.data);

            setLogs(res.content ?? []);
            setTotal(res.totalElements ?? 0);
        } catch (err: any) {
            message.error(err.response?.data?.message || 'Ошибка загрузки аудита');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadTenants();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    useEffect(() => {
        loadLogs();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [page, pageSize, tenantFilter, actionFilter, range[0].valueOf(), range[1].valueOf()]);

    const tenantLabel = (tenantId: string | null): string => {
        if (!tenantId) return 'платформа';
        const t = tenants.find((x) => x.id === tenantId);
        return t ? `${t.name} (${t.slug})` : tenantId.slice(0, 8);
    };

    const columns = [
        {
            title: 'Время',
            dataIndex: 'createdAt',
            key: 'createdAt',
            width: 170,
            render: (v: string) => dayjs(v).format('DD.MM.YYYY HH:mm:ss'),
        },
        {
            title: 'Тенант',
            dataIndex: 'tenantId',
            key: 'tenantId',
            width: 220,
            render: (v: string | null) => v
                ? <Tag color="blue">{tenantLabel(v)}</Tag>
                : <Tag>платформа</Tag>,
        },
        {
            title: 'Пользователь',
            dataIndex: 'username',
            key: 'username',
            width: 160,
            render: (v: string | null) => v ?? '—',
        },
        {
            title: 'Действие',
            dataIndex: 'action',
            key: 'action',
            width: 180,
            render: (v: string) => <Tag color={ACTION_COLORS[v] ?? 'default'}>{v}</Tag>,
        },
        {
            title: 'Цель',
            dataIndex: 'target',
            key: 'target',
            width: 220,
            render: (v: string | null) => v ? <code>{v}</code> : '—',
        },
        {
            title: 'IP',
            dataIndex: 'ip',
            key: 'ip',
            width: 130,
            render: (v: string | null) => <code>{v ?? '—'}</code>,
        },
        {
            title: 'Metadata',
            dataIndex: 'metadata',
            key: 'metadata',
            render: (v: string | null) => v ? <code style={{ fontSize: 11 }}>{v}</code> : '—',
        },
    ];

    return (
        <div>
            <Title level={2} style={{ color: 'white' }}>Логи действий</Title>

            <Alert
                type="info"
                showIcon
                style={{ marginBottom: 16, background: '#1e1e2f', border: '1px solid #3d3d4f' }}
                message="Аудит действий пользователей. Сырые технические логи (SQL, ошибки) — в Grafana (http://localhost:3001)."
            />

            <Card style={{ background: '#2d2d3f', border: 'none', marginBottom: 16 }}>
                <Space wrap>
                    <Select
                        placeholder="Все тенанты"
                        allowClear
                        style={{ width: 260 }}
                        value={tenantFilter}
                        onChange={(v) => { setTenantFilter(v); setPage(1); }}
                        options={tenants.map((t) => ({ value: t.id, label: `${t.name} (${t.slug})` }))}
                    />
                    <Select
                        placeholder="Все действия"
                        allowClear
                        style={{ width: 200 }}
                        value={actionFilter}
                        onChange={(v) => { setActionFilter(v); setPage(1); }}
                        options={ALL_ACTIONS.map((a) => ({ value: a, label: a }))}
                    />
                    <RangePicker
                        value={range}
                        onChange={(v) => {
                            if (v && v[0] && v[1]) {
                                setRange([v[0], v[1]]);
                                setPage(1);
                            }
                        }}
                        format="DD.MM.YYYY"
                    />
                    <Button icon={<ReloadOutlined />} onClick={loadLogs} loading={loading}>
                        Обновить
                    </Button>
                </Space>
            </Card>

            <Card style={{ background: '#2d2d3f', border: 'none' }} styles={{ body: { padding: 0 } }}>
                <Table<AuditEvent>
                    rowKey="id"
                    dataSource={logs}
                    columns={columns}
                    loading={loading}
                    pagination={{
                        current: page,
                        pageSize,
                        total,
                        showSizeChanger: true,
                        onChange: (p, ps) => {
                            setPage(p);
                            setPageSize(ps);
                        },
                    }}
                />
            </Card>

            <Text style={{ color: '#888', fontSize: 12, display: 'block', marginTop: 12 }}>
                Всего событий: {total}. Сырые логи — в Grafana (http://localhost:3001, data source Loki).
            </Text>
        </div>
    );
};

export default PlatformLogs;