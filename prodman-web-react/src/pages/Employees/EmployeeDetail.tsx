import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    Typography, Card, Descriptions, Tag, Spin, Alert, Button, Space,
    Tabs, Table, Modal, Form, Select, InputNumber, Input, Popconfirm, message,
} from 'antd';
import { ArrowLeftOutlined, PlusOutlined, DeleteOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';

import { employeesApi } from '../../api/employees.api';
import type { Employee, EmployeeStatus } from '../../api/employees.api';
import { qualificationsApi } from '../../api/qualifications.api';
import type {
    EmployeeQualification,
    Qualification,
} from '../../api/qualifications.api';
import { positionsApi } from '../../api/positions.api';
import type { Position } from '../../api/positions.api';

const { Title } = Typography;

const STATUS_OPTIONS: { value: EmployeeStatus; label: string; color: string }[] = [
    { value: 'AVAILABLE', label: 'Доступен', color: 'green' },
    { value: 'ACTIVE', label: 'Работает', color: 'blue' },
    { value: 'ON_LEAVE', label: 'В отпуске', color: 'orange' },
    { value: 'FIRED', label: 'Уволен', color: 'red' },
];

const phoneToDisplay = (e164: string | null | undefined): string => {
    if (!e164) return '—';
    if (e164.startsWith('+7') && e164.length === 12) {
        const d = e164.slice(2);
        return `+7 ${d.slice(0, 3)} ${d.slice(3, 6)} ${d.slice(6, 8)} ${d.slice(8, 10)}`;
    }
    return e164;
};

const EmployeeDetail = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();

    const [employee, setEmployee] = useState<Employee | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    // Справочники
    const [positions, setPositions] = useState<Position[]>([]);
    const [allQualifications, setAllQualifications] = useState<Qualification[]>([]);

    // Назначенные квалификации
    const [assigned, setAssigned] = useState<EmployeeQualification[]>([]);
    const [assignLoading, setAssignLoading] = useState(false);

    // Модалка «назначить квалификацию»
    const [modalOpen, setModalOpen] = useState(false);
    const [saving, setSaving] = useState(false);
    const [form] = Form.useForm();

    const loadEmployee = async () => {
        if (!id) return;
        setLoading(true);
        setError('');
        try {
            const data = await employeesApi.getById(id);
            setEmployee(data);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Сотрудник не найден');
        } finally {
            setLoading(false);
        }
    };

    const loadAssigned = async () => {
        if (!id) return;
        setAssignLoading(true);
        try {
            const data = await qualificationsApi.listForEmployee(id);
            setAssigned(data ?? []);
        } catch (err: any) {
            message.error(err.response?.data?.message || 'Ошибка загрузки квалификаций');
        } finally {
            setAssignLoading(false);
        }
    };

    const loadDictionaries = async () => {
        try {
            const [pos, quals] = await Promise.all([
                positionsApi.getAll(0, 200),
                qualificationsApi.getAll(0, 500),
            ]);
            setPositions(pos.content ?? []);
            setAllQualifications(quals.content ?? []);
        } catch {
            message.error('Ошибка загрузки справочников');
        }
    };

    useEffect(() => {
        loadEmployee();
        loadAssigned();
        loadDictionaries();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [id]);

    const handleOpenAssign = () => {
        form.resetFields();
        form.setFieldsValue({ level: 1 });
        setModalOpen(true);
    };

    const handleAssign = async () => {
        if (!id) return;
        try {
            const values = await form.validateFields();
            setSaving(true);
            await qualificationsApi.assign(id, {
                qualificationId: values.qualificationId,
                level: values.level,
                notes: values.notes,
            });
            message.success('Квалификация назначена');
            setModalOpen(false);
            form.resetFields();
            loadAssigned();
        } catch (err: any) {
            if (err.errorFields) return;
            message.error(err.response?.data?.message || 'Ошибка назначения');
        } finally {
            setSaving(false);
        }
    };

    const handleRevoke = async (qualificationId: string) => {
        if (!id) return;
        try {
            await qualificationsApi.revoke(id, qualificationId);
            message.success('Квалификация отозвана');
            loadAssigned();
        } catch (err: any) {
            message.error(err.response?.data?.message || 'Ошибка отзыва');
        }
    };

    if (loading) {
        return <Spin size="large" style={{ display: 'block', marginTop: 100 }} />;
    }

    if (error || !employee) {
        return (
            <div>
                <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/employees')}>
                    Назад
                </Button>
                <Alert
                    message={error || 'Сотрудник не найден'}
                    type="error"
                    showIcon
                    style={{ marginTop: 16 }}
                />
            </div>
        );
    }

    const statusOpt = STATUS_OPTIONS.find((s) => s.value === employee.status);

    // Квалификации, соответствующие должности сотрудника
    const positionFilteredQualifications = employee.positionId
        ? allQualifications.filter((q) => q.positionId === employee.positionId)
        : allQualifications;

    const assignedColumns = [
        { title: 'Код', dataIndex: 'qualificationCode', key: 'qualificationCode', width: 140 },
        { title: 'Название', dataIndex: 'qualificationName', key: 'qualificationName' },
        {
            title: 'Уровень',
            dataIndex: 'level',
            key: 'level',
            width: 100,
            render: (v: number) => <Tag color="blue">{v}</Tag>,
        },
        {
            title: 'Дата присвоения',
            dataIndex: 'assignedAt',
            key: 'assignedAt',
            width: 160,
            render: (v: string) => (v ? dayjs(v).format('DD.MM.YYYY') : '—'),
        },
        { title: 'Заметки', dataIndex: 'notes', key: 'notes' },
        {
            title: 'Действия',
            key: 'actions',
            width: 100,
            render: (_: unknown, record: EmployeeQualification) => (
                <Popconfirm
                    title="Отозвать квалификацию?"
                    onConfirm={() => handleRevoke(record.qualificationId)}
                    okText="Да"
                    cancelText="Нет"
                >
                    <Button icon={<DeleteOutlined />} size="small" danger />
                </Popconfirm>
            ),
        },
    ];

    return (
        <div>
            <Space style={{ marginBottom: 16 }}>
                <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/employees')}>
                    Назад
                </Button>
            </Space>

            <Title level={2} style={{ color: 'white', marginTop: 0 }}>
                {employee.lastName} {employee.firstName} {employee.middleName ?? ''}
            </Title>

            <Card style={{ background: '#2d2d3f', border: 'none', marginBottom: 16 }}>
                <Descriptions column={2} labelStyle={{ color: '#aaa' }} contentStyle={{ color: 'white' }}>
                    <Descriptions.Item label="Табельный">{employee.code}</Descriptions.Item>
                    <Descriptions.Item label="Телефон">{phoneToDisplay(employee.phone)}</Descriptions.Item>
                    <Descriptions.Item label="Отдел">
                        {employee.departmentName ? <Tag color="blue">{employee.departmentName}</Tag> : '—'}
                    </Descriptions.Item>
                    <Descriptions.Item label="Должность">
                        {employee.positionName ? <Tag color="green">{employee.positionName}</Tag> : '—'}
                    </Descriptions.Item>
                    <Descriptions.Item label="Статус">
                        <Tag color={statusOpt?.color || 'default'}>{statusOpt?.label || employee.status}</Tag>
                    </Descriptions.Item>
                    <Descriptions.Item label="Макс. часов подряд">
                        {employee.maxConsecutiveHours}
                    </Descriptions.Item>
                    <Descriptions.Item label="Дата приёма">
                        {employee.hiredAt ? dayjs(employee.hiredAt).format('DD.MM.YYYY') : '—'}
                    </Descriptions.Item>
                    <Descriptions.Item label="Дата увольнения">
                        {employee.firedAt ? dayjs(employee.firedAt).format('DD.MM.YYYY') : '—'}
                    </Descriptions.Item>
                </Descriptions>
            </Card>

            <Tabs
                defaultActiveKey="qualifications"
                items={[
                    {
                        key: 'qualifications',
                        label: 'Квалификации',
                        children: (
                            <div>
                                <Space style={{ marginBottom: 16 }}>
                                    <Button
                                        type="primary"
                                        icon={<PlusOutlined />}
                                        onClick={handleOpenAssign}
                                        style={{ background: '#2ecc71', borderColor: '#2ecc71' }}
                                    >
                                        Назначить квалификацию
                                    </Button>
                                </Space>

                                <Table
                                    dataSource={assigned}
                                    columns={assignedColumns}
                                    rowKey="id"
                                    loading={assignLoading}
                                    pagination={false}
                                />
                            </div>
                        ),
                    },
                ]}
            />

            <Modal
                title="Назначить квалификацию"
                open={modalOpen}
                onOk={handleAssign}
                onCancel={() => setModalOpen(false)}
                confirmLoading={saving}
                okText="Назначить"
                cancelText="Отмена"
                width={500}
            >
                <Form form={form} layout="vertical">
                    <Form.Item
                        name="qualificationId"
                        label="Квалификация"
                        rules={[{ required: true, message: 'Выберите квалификацию' }]}
                    >
                        <Select
                            placeholder="Выберите квалификацию"
                            showSearch
                            optionFilterProp="label"
                            options={positionFilteredQualifications.map((q) => {
                                const pos = positions.find((p) => p.id === q.positionId);
                                return {
                                    value: q.id,
                                    label: `${q.code} — ${q.name}${pos ? ` (${pos.name})` : ''}`,
                                };
                            })}
                        />
                    </Form.Item>

                    <Form.Item
                        name="level"
                        label="Уровень владения"
                        tooltip="1 — начинающий, выше — опытнее"
                        rules={[{ required: true, message: 'Укажите уровень' }]}
                    >
                        <InputNumber min={1} max={10} style={{ width: '100%' }} />
                    </Form.Item>

                    <Form.Item name="notes" label="Заметки">
                        <Input.TextArea rows={3} placeholder="Например: аттестован 01.10.2026" />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default EmployeeDetail;