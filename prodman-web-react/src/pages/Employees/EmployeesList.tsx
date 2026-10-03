import { useEffect, useState } from 'react';
import {
  Typography, Table, Tag, Spin, Alert, Button, Space, Input, Modal, Form,
  Popconfirm, message, Select, InputNumber, DatePicker,
} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';

import { employeesApi } from '../../api/employees.api';
import type { Employee, CreateEmployeeRequest, EmployeeStatus } from '../../api/employees.api';
import { positionsApi } from '../../api/positions.api';
import type { Position } from '../../api/positions.api';
import { departmentsApi } from '../../api/departments.api';
import type { Department } from '../../api/departments.api';

const { Title } = Typography;

const STATUS_OPTIONS: { value: EmployeeStatus; label: string; color: string }[] = [
  { value: 'AVAILABLE', label: 'Доступен', color: 'green' },
  { value: 'ACTIVE', label: 'Работает', color: 'blue' },
  { value: 'ON_LEAVE', label: 'В отпуске', color: 'orange' },
  { value: 'FIRED', label: 'Уволен', color: 'red' },
];

/** Backend хранит телефон в E.164 (+71231231212). В UI показываем 10 цифр. */
const phoneToInput = (e164: string | null | undefined): string =>
    e164 && e164.startsWith('+7') ? e164.slice(2) : (e164 || '');

const EmployeesList = () => {
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [positions, setPositions] = useState<Position[]>([]);
  const [departments, setDepartments] = useState<Department[]>([]);

  const [modalOpen, setModalOpen] = useState(false);
  const [editingEmployee, setEditingEmployee] = useState<Employee | null>(null);
  const [saving, setSaving] = useState(false);
  const [form] = Form.useForm();

  const loadEmployees = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await employeesApi.getAll(0, 200);
      setEmployees(data.content ?? []);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка загрузки');
    } finally {
      setLoading(false);
    }
  };

  const loadDictionaries = async () => {
    try {
      const [pos, dep] = await Promise.all([
        positionsApi.getAll(0, 200),
        departmentsApi.getAll(0, 200),
      ]);
      setPositions(pos.content ?? []);
      setDepartments(dep.content ?? []);
    } catch {
      message.error('Ошибка загрузки справочников');
    }
  };

  useEffect(() => {
    loadEmployees();
    loadDictionaries();
  }, []);

  const handleCreate = () => {
    setEditingEmployee(null);
    form.resetFields();
    form.setFieldsValue({ maxConsecutiveHours: 12, status: 'AVAILABLE' });
    setModalOpen(true);
  };

  const handleEdit = (employee: Employee) => {
    setEditingEmployee(employee);
    form.setFieldsValue({
      code: employee.code,
      firstName: employee.firstName,
      lastName: employee.lastName,
      middleName: employee.middleName ?? '',
      phone: phoneToInput(employee.phone),
      departmentId: employee.departmentId ?? undefined,
      positionId: employee.positionId ?? undefined,
      hiredAt: employee.hiredAt ? dayjs(employee.hiredAt) : undefined,
      firedAt: employee.firedAt ? dayjs(employee.firedAt) : undefined,
      status: employee.status,
      maxConsecutiveHours: employee.maxConsecutiveHours,
    });
    setModalOpen(true);
  };

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      setSaving(true);

      const payload: CreateEmployeeRequest = {
        code: values.code,
        firstName: values.firstName,
        lastName: values.lastName,
        middleName: values.middleName || undefined,
        phone: values.phone,
        departmentId: values.departmentId || undefined,
        positionId: values.positionId || undefined,
        hiredAt: values.hiredAt ? values.hiredAt.format('YYYY-MM-DD') : undefined,
        firedAt: values.firedAt ? values.firedAt.format('YYYY-MM-DD') : undefined,
        maxConsecutiveHours: values.maxConsecutiveHours,
      };

      if (editingEmployee) {
        await employeesApi.update(editingEmployee.id, payload);
        message.success('Сотрудник обновлён');
      } else {
        await employeesApi.create(payload);
        message.success('Сотрудник создан');
      }

      setModalOpen(false);
      form.resetFields();
      loadEmployees();
    } catch (err: any) {
      if (err.errorFields) return;
      message.error(err.response?.data?.message || 'Ошибка сохранения');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await employeesApi.delete(id);
      message.success('Сотрудник удалён');
      loadEmployees();
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Ошибка удаления');
    }
  };

  const columns = [
    { title: 'Табельный', dataIndex: 'code', key: 'code', width: 120 },
    { title: 'Фамилия', dataIndex: 'lastName', key: 'lastName' },
    { title: 'Имя', dataIndex: 'firstName', key: 'firstName' },
    {
      title: 'Телефон',
      dataIndex: 'phone',
      key: 'phone',
      render: (p: string) => phoneToInput(p),
    },
    {
      title: 'Отдел',
      dataIndex: 'departmentName',
      key: 'departmentName',
      render: (n: string | null) => (n ? <Tag color="blue">{n}</Tag> : '—'),
    },
    {
      title: 'Должность',
      dataIndex: 'positionName',
      key: 'positionName',
      render: (n: string | null) => (n ? <Tag color="green">{n}</Tag> : '—'),
    },
    {
      title: 'Статус',
      dataIndex: 'status',
      key: 'status',
      render: (status: EmployeeStatus) => {
        const opt = STATUS_OPTIONS.find((o) => o.value === status);
        return <Tag color={opt?.color || 'default'}>{opt?.label || status}</Tag>;
      },
    },
    {
      title: 'Действия',
      key: 'actions',
      render: (_: unknown, record: Employee) => (
          <Space>
            <Button icon={<EditOutlined />} size="small" onClick={() => handleEdit(record)} />
            <Popconfirm
                title="Удалить сотрудника?"
                description="Это действие нельзя отменить"
                onConfirm={() => handleDelete(record.id)}
                okText="Да"
                cancelText="Нет"
            >
              <Button icon={<DeleteOutlined />} size="small" danger />
            </Popconfirm>
          </Space>
      ),
    },
  ];

  if (loading && employees.length === 0) {
    return <Spin size="large" style={{ display: 'block', marginTop: 100 }} />;
  }

  return (
      <div>
        <Title level={2} style={{ color: 'white' }}>
          Управление работниками
        </Title>

        <Space style={{ marginBottom: 16 }} wrap>
          <Button onClick={loadEmployees} icon={<ReloadOutlined />}>
            Обновить
          </Button>
          <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={handleCreate}
              style={{ background: '#2ecc71', borderColor: '#2ecc71' }}
          >
            Создать
          </Button>
        </Space>

        {error && <Alert message={error} type="error" showIcon style={{ marginBottom: 16 }} />}

        <Table
            dataSource={employees}
            columns={columns}
            rowKey="id"
            loading={loading}
            pagination={{ pageSize: 20 }}
        />

        <Modal
            title={editingEmployee ? 'Редактирование сотрудника' : 'Новый сотрудник'}
            open={modalOpen}
            onOk={handleSave}
            onCancel={() => setModalOpen(false)}
            confirmLoading={saving}
            okText="Сохранить"
            cancelText="Отмена"
            width={700}
        >
          <Form form={form} layout="vertical">
            <Form.Item
                name="code"
                label="Табельный номер"
                rules={[{ required: true, message: 'Введите табельный номер' }]}
            >
              <Input placeholder="Например: EMP-001" />
            </Form.Item>

            <Form.Item
                name="lastName"
                label="Фамилия"
                rules={[{ required: true, message: 'Введите фамилию' }]}
            >
              <Input />
            </Form.Item>

            <Form.Item
                name="firstName"
                label="Имя"
                rules={[{ required: true, message: 'Введите имя' }]}
            >
              <Input />
            </Form.Item>

            <Form.Item name="middleName" label="Отчество">
              <Input />
            </Form.Item>

            <Form.Item
                name="phone"
                label="Телефон"
                tooltip="10 цифр без +7, например 1231231212"
                rules={[
                  { required: true, message: 'Введите телефон' },
                  { pattern: /^\d{10}$/, message: 'Ровно 10 цифр без +7' },
                ]}
            >
              <Input placeholder="1231231212" maxLength={10} />
            </Form.Item>

            <Form.Item name="departmentId" label="Отдел">
              <Select
                  placeholder="Выберите отдел"
                  allowClear
                  options={departments.map((d) => ({ value: d.id, label: `${d.code} — ${d.name}` }))}
              />
            </Form.Item>

            <Form.Item name="positionId" label="Должность">
              <Select
                  placeholder="Выберите должность"
                  allowClear
                  options={positions.map((p) => ({ value: p.id, label: `${p.code} — ${p.name}` }))}
              />
            </Form.Item>

            <Form.Item name="hiredAt" label="Дата приёма">
              <DatePicker style={{ width: '100%' }} format="YYYY-MM-DD" />
            </Form.Item>

            <Form.Item name="firedAt" label="Дата увольнения">
              <DatePicker style={{ width: '100%' }} format="YYYY-MM-DD" />
            </Form.Item>

            <Form.Item
                name="maxConsecutiveHours"
                label="Максимум часов подряд"
                tooltip="По умолчанию 12 часов"
            >
              <InputNumber min={1} max={24} style={{ width: '100%' }} />
            </Form.Item>
          </Form>
        </Modal>
      </div>
  );
};

export default EmployeesList;