import { useEffect, useState } from 'react';
import {
  Typography,
  Table,
  Tag,
  Spin,
  Alert,
  Button,
  Space,
  Input,
  Modal,
  Form,
  Popconfirm,
  message,
  Select,
  InputNumber,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
} from '@ant-design/icons';

import { employeesApi } from '../../api/employees.api';
import type { Employee, CreateEmployeeRequest, EmployeeStatus } from '../../api/employees.api';
import { positionsApi } from '../../api/positions.api';
import type { Position } from '../../api/positions.api';
import { departmentsApi } from '../../api/departments.api';
import type { Department } from '../../api/departments.api';

const { Title } = Typography;

const STATUS_OPTIONS: { value: EmployeeStatus; label: string; color: string }[] = [
  { value: 'AVAILABLE', label: 'Доступен', color: 'green' },
  { value: 'BUSY', label: 'Занят', color: 'blue' },
  { value: 'VACATION', label: 'В отпуске', color: 'orange' },
  { value: 'SICK_LEAVE', label: 'Больничный', color: 'red' },
  { value: 'UNAVAILABLE', label: 'Недоступен', color: 'gray' },
];

const EmployeesList = () => {
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [positions, setPositions] = useState<Position[]>([]);
  const [departments, setDepartments] = useState<Department[]>([]);

  const [searchEmail, setSearchEmail] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [editingEmployee, setEditingEmployee] = useState<Employee | null>(null);
  const [saving, setSaving] = useState(false);
  const [form] = Form.useForm();

  // === Загрузка данных ===
  const loadEmployees = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await employeesApi.getAll();
      setEmployees(Array.isArray(data) ? data : []);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка загрузки');
    } finally {
      setLoading(false);
    }
  };

  const loadDictionaries = async () => {
    try {
      const [pos, dep] = await Promise.all([
        positionsApi.getAll(),
        departmentsApi.getAll(),
      ]);
      setPositions(pos);
      setDepartments(dep);
    } catch (err) {
      message.error('Ошибка загрузки справочников');
    }
  };

  useEffect(() => {
    loadEmployees();
    loadDictionaries();
  }, []);

  // === Поиск ===
  const handleSearch = async () => {
    if (!searchEmail.trim()) {
      loadEmployees();
      return;
    }
    setLoading(true);
    setError('');
    try {
      const data = await employeesApi.getByEmail(searchEmail.trim());
      setEmployees([data]);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Сотрудник не найден');
      setEmployees([]);
    } finally {
      setLoading(false);
    }
  };

  const handleResetSearch = () => {
    setSearchEmail('');
    loadEmployees();
  };

  // === Модалка ===
  const handleCreate = () => {
    setEditingEmployee(null);
    form.resetFields();
    form.setFieldsValue({ maxConsecutiveHours: 12 });
    setModalOpen(true);
  };

  const handleEdit = (employee: Employee) => {
    setEditingEmployee(employee);
    form.setFieldsValue({
      firstName: employee.firstName,
      lastName: employee.lastName,
      email: employee.email,
      phoneNumber: employee.phoneNumber,
      positionId: employee.position?.id,
      departmentId: employee.department?.id,
      status: employee.status,
      maxConsecutiveHours: employee.maxConsecutiveHours,
    });
    setModalOpen(true);
  };

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      setSaving(true);

      if (editingEmployee) {
        await employeesApi.update(editingEmployee.id, values);
        message.success('Сотрудник обновлён');
      } else {
        await employeesApi.create(values as CreateEmployeeRequest);
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

  // === Колонки ===
  const columns = [
    { title: 'Имя', dataIndex: 'firstName', key: 'firstName' },
    { title: 'Фамилия', dataIndex: 'lastName', key: 'lastName' },
    { title: 'Email', dataIndex: 'email', key: 'email' },
    {
      title: 'Отдел',
      dataIndex: 'department',
      key: 'department',
      render: (dep: Employee['department']) =>
          dep ? (
              <Tag color={dep.color}>{dep.name}</Tag>
          ) : (
              '—'
          ),
    },
    {
      title: 'Должность',
      dataIndex: 'position',
      key: 'position',
      render: (pos: Employee['position']) =>
          pos ? (
              <Tag color={pos.color}>{pos.name}</Tag>
          ) : (
              '—'
          ),
    },
    {
      title: 'Статус',
      dataIndex: 'status',
      key: 'status',
      render: (status: EmployeeStatus) => {
        const opt = STATUS_OPTIONS.find(o => o.value === status);
        return <Tag color={opt?.color || 'default'}>{opt?.label || status}</Tag>;
      },
    },
    {
      title: 'Действия',
      key: 'actions',
      render: (_: any, record: Employee) => (
          <Space>
            <Button
                icon={<EditOutlined />}
                size="small"
                onClick={() => handleEdit(record)}
            />
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
          <Input
              placeholder="Поиск по email"
              value={searchEmail}
              onChange={(e) => setSearchEmail(e.target.value)}
              onPressEnter={handleSearch}
              style={{ width: 250 }}
              prefix={<SearchOutlined />}
          />
          <Button type="primary" onClick={handleSearch}>
            Найти
          </Button>
          <Button onClick={handleResetSearch} icon={<ReloadOutlined />}>
            Сбросить
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

        {error && (
            <Alert message={error} type="error" showIcon style={{ marginBottom: 16 }} />
        )}

        <Table
            dataSource={employees}
            columns={columns}
            rowKey="id"
            loading={loading}
            pagination={{ pageSize: 10 }}
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
                name="firstName"
                label="Имя"
                rules={[{ required: true, message: 'Введите имя' }]}
            >
              <Input />
            </Form.Item>

            <Form.Item
                name="lastName"
                label="Фамилия"
                rules={[{ required: true, message: 'Введите фамилию' }]}
            >
              <Input />
            </Form.Item>

            <Form.Item
                name="email"
                label="Email"
                rules={[
                  { required: true, message: 'Введите email' },
                  { type: 'email', message: 'Некорректный email' },
                ]}
            >
              <Input disabled={!!editingEmployee} />
            </Form.Item>

            <Form.Item name="phoneNumber" label="Телефон">
              <Input />
            </Form.Item>

            <Form.Item name="departmentId" label="Отдел">
              <Select
                  placeholder="Выберите отдел"
                  allowClear
                  options={departments.map(d => ({
                    value: d.id,
                    label: d.name,
                  }))}
              />
            </Form.Item>

            <Form.Item name="positionId" label="Должность">
              <Select
                  placeholder="Выберите должность"
                  allowClear
                  options={positions.map(p => ({
                    value: p.id,
                    label: p.name,
                  }))}
              />
            </Form.Item>

            {editingEmployee && (
                <Form.Item name="status" label="Статус">
                  <Select
                      placeholder="Выберите статус"
                      options={STATUS_OPTIONS.map(s => ({
                        value: s.value,
                        label: s.label,
                      }))}
                  />
                </Form.Item>
            )}

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