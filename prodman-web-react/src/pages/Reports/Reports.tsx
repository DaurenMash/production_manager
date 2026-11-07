import { useEffect, useMemo, useState } from 'react';
import {
    Typography, Space, DatePicker, Select, Button, Table, message, Card, Tag, Tabs,
} from 'antd';
import { DownloadOutlined, ReloadOutlined } from '@ant-design/icons';
import { useSearchParams } from 'react-router-dom'; // <--- добавили
import dayjs, { Dayjs } from 'dayjs';
import 'dayjs/locale/ru';
import { workCalendarApi } from '../../api/workCalendar.api';
import type { EmployeeHoursReportRow } from '../../api/workCalendar.api';
import { departmentsApi } from '../../api/departments.api';
import type { Department } from '../../api/departments.api';
import type { ScheduleResponse } from '../../types/schedule.types';
import ScheduleGrid from '../Calendar/ScheduleGrid';

dayjs.locale('ru');
const { Title, Text } = Typography;
const { RangePicker } = DatePicker;

// ==========================================
// Вкладка 1: График работы (ScheduleGrid)
// ==========================================
const ScheduleTab = () => {
    const [searchParams] = useSearchParams(); // <--- читаем URL

    const [data, setData] = useState<ScheduleResponse | null>(null);
    const [loading, setLoading] = useState(false);
    const [departments, setDepartments] = useState<Department[]>([]);

    // Инициализируем состояние из URL, если там что-то есть
    const initDept = searchParams.get('departmentId') || undefined;
    const initMonth = searchParams.get('month')
        ? dayjs(searchParams.get('month'), 'YYYY-MM')
        : dayjs();

    const [departmentId, setDepartmentId] = useState<string | undefined>(initDept);
    const [selectedMonth, setSelectedMonth] = useState<Dayjs>(initMonth);

    useEffect(() => {
        departmentsApi.getAll(0, 500).then(res => setDepartments(res.content ?? [])).catch(() => {});
    }, []);

    const loadSchedule = async () => {
        setLoading(true);
        try {
            const from = selectedMonth.startOf('month').format('YYYY-MM-DD');
            const to = selectedMonth.endOf('month').format('YYYY-MM-DD');
            const res = await workCalendarApi.getSchedule(from, to, departmentId);
            setData(res);
        } catch {
            message.error('Ошибка загрузки графика');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadSchedule();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [selectedMonth.valueOf(), departmentId]);

    return (
        <div>
            <Space style={{ marginBottom: 16 }} wrap>
                <DatePicker
                    picker="month"
                    value={selectedMonth}
                    onChange={(v) => { if (v) setSelectedMonth(v); }}
                    format="MMMM YYYY"
                    allowClear={false}
                />
                <Select
                    placeholder="Все отделы"
                    allowClear
                    style={{ width: 220 }}
                    value={departmentId}
                    onChange={setDepartmentId}
                    options={departments.map((d) => ({ value: d.id, label: d.name }))}
                />
                <Button icon={<ReloadOutlined />} onClick={loadSchedule}>
                    Обновить
                </Button>
            </Space>
            <Card style={{ background: '#2d2d3f', border: 'none' }} styles={{ body: { padding: 12 } }}>
                <ScheduleGrid data={data} loading={loading} />
            </Card>
        </div>
    );
};

// ==========================================
// Вкладка 2: Часы по сотрудникам (старый отчёт)
// ==========================================
const HoursByEmployeeTab = () => {
    const [data, setData] = useState<EmployeeHoursReportRow[]>([]);
    const [departments, setDepartments] = useState<Department[]>([]);
    const [loading, setLoading] = useState(false);
    const [range, setRange] = useState<[Dayjs, Dayjs]>([
        dayjs().startOf('month'),
        dayjs().endOf('month'),
    ]);
    const [departmentId, setDepartmentId] = useState<string | undefined>();

    const loadDepartments = async () => {
        try {
            const dep = await departmentsApi.getAll(0, 500);
            setDepartments(dep.content ?? []);
        } catch {
            // silent
        }
    };

    const loadReport = async () => {
        setLoading(true);
        try {
            const rows = await workCalendarApi.reportByEmployee(
                range[0].format('YYYY-MM-DD'),
                range[1].format('YYYY-MM-DD'),
            );
            setData(rows);
        } catch {
            message.error('Ошибка загрузки отчёта');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadDepartments();
    }, []);

    useEffect(() => {
        loadReport();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [range[0].valueOf(), range[1].valueOf()]);

    const filtered = useMemo(() => {
        if (!departmentId) return data;
        return data.filter((r) => r.employeeDepartmentId === departmentId);
    }, [data, departmentId]);

    const departmentName = (id: string | null): string => {
        if (!id) return '—';
        return departments.find((d) => d.id === id)?.name ?? '—';
    };

    const exportToCsv = () => {
        if (filtered.length === 0) {
            message.warning('Нет данных для выгрузки');
            return;
        }
        const header = [
            'Сотрудник', 'Отдел', 'Всего часов', 'Обычные', 'Ночные', 'Выходные часы',
            'Смен', 'Смен в выходной',
            `Коэф. база (${filtered[0]?.baseCoefficientName ?? ''})`,
            `Коэф. выходной (${filtered[0]?.weekendCoefficientName ?? ''})`,
            `Коэф. ночь (${filtered[0]?.nightCoefficientName ?? ''})`,
        ];
        const rows = filtered.map((r) => [
            r.employeeFullName ?? '',
            departmentName(r.employeeDepartmentId),
            r.totalHours,
            r.normalHours,
            r.nightHours,
            r.dayOffHours,
            r.assignmentsCount,
            r.dayOffAssignmentsCount,
            r.baseCoefficientValue,
            r.weekendCoefficientValue,
            r.nightCoefficientValue,
        ]);

        const csv = [header, ...rows]
            .map((row) => row.map((v) => `"${String(v).replace(/"/g, '""')}"`).join(';'))
            .join('\r\n');

        const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `hours_${range[0].format('YYYY-MM-DD')}_${range[1].format('YYYY-MM-DD')}.csv`;
        a.click();
        URL.revokeObjectURL(url);
    };

    const columns = [
        {
            title: 'Сотрудник',
            dataIndex: 'employeeFullName',
            key: 'employeeFullName',
            render: (v: string | null) => v ?? '—',
        },
        {
            title: 'Отдел',
            dataIndex: 'employeeDepartmentId',
            key: 'department',
            render: (v: string | null) => departmentName(v),
        },
        {
            title: 'Всего часов',
            dataIndex: 'totalHours',
            key: 'totalHours',
            align: 'right' as const,
            render: (v: number) => <b>{v}</b>,
        },
        {
            title: 'Обычные',
            dataIndex: 'normalHours',
            key: 'normalHours',
            align: 'right' as const,
        },
        {
            title: 'Ночные',
            dataIndex: 'nightHours',
            key: 'nightHours',
            align: 'right' as const,
            render: (v: number) => Number(v) > 0
                ? <Tag color="purple">{v}</Tag>
                : <span style={{ color: '#666' }}>0</span>,
        },
        {
            title: 'В выходной',
            dataIndex: 'dayOffHours',
            key: 'dayOffHours',
            align: 'right' as const,
            render: (v: number) => Number(v) > 0
                ? <Tag color="orange">{v}</Tag>
                : <span style={{ color: '#666' }}>0</span>,
        },
        {
            title: 'Смен',
            dataIndex: 'assignmentsCount',
            key: 'assignmentsCount',
            align: 'right' as const,
        },
        {
            title: 'Смен в выходной',
            dataIndex: 'dayOffAssignmentsCount',
            key: 'dayOffAssignmentsCount',
            align: 'right' as const,
            render: (v: number) => Number(v) > 0
                ? <Tag color="orange">{v}</Tag>
                : <span style={{ color: '#666' }}>0</span>,
        },
    ];

    return (
        <div>
            <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }} wrap>
                <Space wrap>
                    <RangePicker
                        value={range}
                        onChange={(v) => {
                            if (v && v[0] && v[1]) setRange([v[0], v[1]]);
                        }}
                        format="DD.MM.YYYY"
                    />
                    <Select
                        placeholder="Все отделы"
                        allowClear
                        style={{ width: 220 }}
                        value={departmentId}
                        onChange={setDepartmentId}
                        options={departments.map((d) => ({ value: d.id, label: d.name }))}
                    />
                    <Button icon={<ReloadOutlined />} onClick={loadReport}>
                        Обновить
                    </Button>
                    <Button
                        type="primary"
                        icon={<DownloadOutlined />}
                        onClick={exportToCsv}
                        style={{ background: '#2ecc71', borderColor: '#2ecc71' }}
                    >
                        Выгрузить CSV
                    </Button>
                </Space>
            </Space>

            <Card
                style={{ background: '#2d2d3f', border: 'none' }}
                styles={{ body: { padding: 0 } }}
            >
                <Table<EmployeeHoursReportRow>
                    rowKey="employeeId"
                    loading={loading}
                    dataSource={filtered}
                    columns={columns}
                    pagination={{ pageSize: 50, showSizeChanger: true }}
                    summary={(rows) => {
                        if (rows.length === 0) return null;
                        const sum = (k: keyof EmployeeHoursReportRow) =>
                            rows.reduce((acc, r) => acc + Number(r[k] ?? 0), 0);
                        return (
                            <Table.Summary.Row style={{ background: '#1e1e2f', fontWeight: 600 }}>
                                <Table.Summary.Cell index={0}>Итого</Table.Summary.Cell>
                                <Table.Summary.Cell index={1} />
                                <Table.Summary.Cell index={2} align="right">
                                    <b>{sum('totalHours').toFixed(2)}</b>
                                </Table.Summary.Cell>
                                <Table.Summary.Cell index={3} align="right">
                                    {sum('normalHours').toFixed(2)}
                                </Table.Summary.Cell>
                                <Table.Summary.Cell index={4} align="right">
                                    {sum('nightHours').toFixed(2)}
                                </Table.Summary.Cell>
                                <Table.Summary.Cell index={5} align="right">
                                    {sum('dayOffHours').toFixed(2)}
                                </Table.Summary.Cell>
                                <Table.Summary.Cell index={6} align="right">
                                    {sum('assignmentsCount')}
                                </Table.Summary.Cell>
                                <Table.Summary.Cell index={7} align="right">
                                    {sum('dayOffAssignmentsCount')}
                                </Table.Summary.Cell>
                            </Table.Summary.Row>
                        );
                    }}
                />
            </Card>

            <Text style={{ color: '#666', fontSize: 12, display: 'block', marginTop: 12 }}>
                «Обычные» = total − выходные (ночные входят в обычные как надбавка).
                HR считает зарплату: обычные × {data[0]?.baseCoefficientValue ?? '?'} +
                выходные × {data[0]?.weekendCoefficientValue ?? '?'} +
                ночные × {data[0]?.nightCoefficientValue ?? '?'}.
            </Text>
        </div>
    );
};


// ==========================================
// Основная страница Отчёты
// ==========================================
const Reports = () => {
    const [searchParams, setSearchParams] = useSearchParams(); // <--- читаем URL
    const tabFromUrl = searchParams.get('tab');

    // Определяем активную вкладку (по умолчанию schedule)
    const activeTab = tabFromUrl === 'hours' ? 'hours' : 'schedule';

    const handleTabChange = (key: string) => {
        // Меняем URL при клике на вкладку
        const newParams = new URLSearchParams(searchParams);
        newParams.set('tab', key);
        setSearchParams(newParams, { replace: true });
    };

    return (
        <div>
            <Title level={3} style={{ color: 'white', marginTop: 0, marginBottom: 24 }}>
                Отчёты
            </Title>
            <Tabs
                activeKey={activeTab}
                onChange={handleTabChange}
                items={[
                    {
                        key: 'schedule',
                        label: 'График работы',
                        children: <ScheduleTab />,
                    },
                    {
                        key: 'hours',
                        label: 'Часы по сотрудникам',
                        children: <HoursByEmployeeTab />,
                    },
                    {
                        key: 'equipment',
                        label: 'Оборудование',
                        children: <Text style={{ color: '#aaa' }}>Этот отчёт будет добавлен позже.</Text>,
                        disabled: true,
                    }
                ]}
            />
        </div>
    );
};

export default Reports;