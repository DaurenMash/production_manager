import { useEffect, useState } from 'react';
import {
    Typography, Space, Card, Table, DatePicker, Button, message, Tabs, Tag,
} from 'antd';
import { ReloadOutlined } from '@ant-design/icons';
import dayjs, { Dayjs } from 'dayjs';
import 'dayjs/locale/ru';

import { workCalendarApi } from '../../api/workCalendar.api';
import type {
    EmployeeHoursReportRow,
    EquipmentReportRow,
} from '../../api/workCalendar.api';
import { employeesApi } from '../../api/employees.api';
import type { Employee } from '../../api/employees.api';
import { departmentsApi } from '../../api/departments.api';
import type { Department } from '../../api/departments.api';

dayjs.locale('ru');

const { Title, Text } = Typography;

const Reports = () => {
    const [month, setMonth] = useState<Dayjs>(dayjs().startOf('month'));

    const [employees, setEmployees] = useState<Employee[]>([]);
    const [departments, setDepartments] = useState<Department[]>([]);

    const [employeeHours, setEmployeeHours] = useState<EmployeeHoursReportRow[]>([]);
    const [equipment, setEquipment] = useState<EquipmentReportRow[]>([]);

    const [loadingHours, setLoadingHours] = useState(false);
    const [loadingEquipment, setLoadingEquipment] = useState(false);

    const loadDictionaries = async () => {
        try {
            const [emp, dep] = await Promise.all([
                employeesApi.getAll(0, 500),
                departmentsApi.getAll(0, 200),
            ]);
            setEmployees(emp.content ?? []);
            setDepartments(dep.content ?? []);
        } catch {
            message.error('Ошибка загрузки справочников');
        }
    };

    const loadHours = async () => {
        setLoadingHours(true);
        try {
            const from = month.startOf('month').format('YYYY-MM-DD');
            const to = month.endOf('month').format('YYYY-MM-DD');
            const data = await workCalendarApi.reportByEmployee(from, to);
            setEmployeeHours(data ?? []);
        } catch {
            message.error('Ошибка загрузки отчёта по часам');
        } finally {
            setLoadingHours(false);
        }
    };

    const loadEquipment = async () => {
        setLoadingEquipment(true);
        try {
            const from = month.startOf('month').format('YYYY-MM-DD');
            const to = month.endOf('month').format('YYYY-MM-DD');
            const data = await workCalendarApi.reportEquipment(from, to);
            setEquipment(data ?? []);
        } catch {
            message.error('Ошибка загрузки отчёта по оборудованию');
        } finally {
            setLoadingEquipment(false);
        }
    };

    useEffect(() => {
        loadDictionaries();
    }, []);

    useEffect(() => {
        loadHours();
        loadEquipment();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [month.format('YYYY-MM')]);

    const employeeNameById = (id: string | null) => {
        if (!id) return '—';
        const e = employees.find((x) => x.id === id);
        return e ? `${e.lastName} ${e.firstName}` : id;
    };

    const departmentNameById = (id: string | null) => {
        if (!id) return '—';
        const d = departments.find((x) => x.id === id);
        return d ? d.name : id;
    };

    const hoursColumns = [
        {
            title: 'Сотрудник',
            dataIndex: 'employeeFullName',
            key: 'employeeFullName',
            render: (v: string | null, r: EmployeeHoursReportRow) =>
                v ?? employeeNameById(r.employeeId),
        },
        {
            title: 'Отдел',
            dataIndex: 'employeeDepartmentId',
            key: 'employeeDepartmentId',
            render: (id: string | null) => departmentNameById(id),
        },
        {
            title: 'Всего часов',
            dataIndex: 'totalHours',
            key: 'totalHours',
            render: (v: number) => <Tag color="blue">{v} ч</Tag>,
        },
        {
            title: 'Ночных',
            dataIndex: 'nightHours',
            key: 'nightHours',
            render: (v: number) => (Number(v) > 0 ? <Tag color="purple">{v} ч</Tag> : '—'),
        },
        {
            title: 'В выходной',
            dataIndex: 'dayOffHours',
            key: 'dayOffHours',
            render: (v: number) => (Number(v) > 0 ? <Tag color="orange">{v} ч</Tag> : '—'),
        },
        {
            title: 'Смен',
            dataIndex: 'assignmentsCount',
            key: 'assignmentsCount',
        },
    ];

    const equipmentColumns = [
        {
            title: 'Станок',
            dataIndex: 'workstationName',
            key: 'workstationName',
            render: (v: string | null, r: EquipmentReportRow) => v ?? r.workstationId,
        },
        {
            title: 'Рабочих дней',
            dataIndex: 'workingDays',
            key: 'workingDays',
            render: (v: number) => <Tag color="green">{v}</Tag>,
        },
        {
            title: 'Смен запланировано',
            dataIndex: 'shiftsPlanned',
            key: 'shiftsPlanned',
        },
        {
            title: 'Всего слотов',
            dataIndex: 'totalSlots',
            key: 'totalSlots',
        },
        {
            title: 'Заполнено',
            dataIndex: 'filledSlots',
            key: 'filledSlots',
            render: (v: number, r: EquipmentReportRow) => (
                <span>
          <Tag color="blue">{v}</Tag>
                    {r.openSlots > 0 && <Tag color="orange">свободно {r.openSlots}</Tag>}
        </span>
            ),
        },
        {
            title: 'Загруженность',
            dataIndex: 'utilization',
            key: 'utilization',
            render: (v: number) => {
                const color = v >= 90 ? 'green' : v >= 60 ? 'orange' : 'red';
                return <Tag color={color}>{v}%</Tag>;
            },
        },
    ];

    return (
        <div>
            <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }} wrap>
                <Title level={3} style={{ color: 'white', margin: 0 }}>
                    Отчёты
                </Title>
                <Space wrap>
                    <Text style={{ color: '#aaa' }}>Месяц:</Text>
                    <DatePicker
                        picker="month"
                        value={month}
                        onChange={(v) => v && setMonth(v)}
                        format="MMMM YYYY"
                        allowClear={false}
                    />
                    <Button
                        icon={<ReloadOutlined />}
                        onClick={() => {
                            loadHours();
                            loadEquipment();
                        }}
                    >
                        Обновить
                    </Button>
                </Space>
            </Space>

            <Tabs
                defaultActiveKey="hours"
                items={[
                    {
                        key: 'hours',
                        label: 'Часы сотрудников',
                        children: (
                            <Card style={{ background: '#2d2d3f', border: 'none' }}>
                                <Table
                                    dataSource={employeeHours}
                                    columns={hoursColumns}
                                    rowKey="employeeId"
                                    loading={loadingHours}
                                    pagination={{ pageSize: 50 }}
                                    locale={{ emptyText: 'Нет данных за выбранный месяц' }}
                                />
                            </Card>
                        ),
                    },
                    {
                        key: 'equipment',
                        label: 'Оборудование',
                        children: (
                            <Card style={{ background: '#2d2d3f', border: 'none' }}>
                                <Table
                                    dataSource={equipment}
                                    columns={equipmentColumns}
                                    rowKey="workstationId"
                                    loading={loadingEquipment}
                                    pagination={{ pageSize: 50 }}
                                    locale={{ emptyText: 'Нет данных за выбранный месяц' }}
                                />
                            </Card>
                        ),
                    },
                ]}
            />
        </div>
    );
};

export default Reports;