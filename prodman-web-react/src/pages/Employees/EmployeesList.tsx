// Импорты React и Ant Design
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
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
} from '@ant-design/icons';

// Импорты нашего API
import { employeesApi } from '../../api/employees.api';
import type { Employee, CreateEmployeeRequest } from '../../api/employees.api';

const { Title } = Typography;

// Список возможных статусов (совпадает с EmployeeStatus.java)
const STATUS_OPTIONS = [
  { value: 'AVAILABLE', label: 'Доступен', color: 'green' },
  { value: 'ON_VACATION', label: 'В отпуске', color: 'orange' },
  { value: 'SICK_LEAVE', label: 'Больничный', color: 'red' },
  { value: 'FIRED', label: 'Уволен', color: 'gray' },
];

const EmployeesList = () => {
  // === Состояния ===
  const [employees, setEmployees] = useState<Employee[]>([]);   // список сотрудников
  const [loading, setLoading] = useState(true);                  // индикатор загрузки
  const [error, setError] = useState('');                        // ошибка загрузки

  const [searchEmail, setSearchEmail] = useState('');            // поиск по email
  const [modalOpen, setModalOpen] = useState(false);             // открыта ли модалка
  const [editingEmployee, setEditingEmployee] = useState<Employee | null>(null); // редактируемый сотрудник
  const [saving, setSaving] = useState(false);                   // сохранение
  const [form] = Form.useForm();                                  // управление формой

  // === Загрузка всех сотрудников ===
  const loadEmployees = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await employeesApi.getAll();
      setEmployees(Array.isArray(data) ? data : [data]);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка загрузки');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadEmployees();
  }, []);

  // === Поиск по email ===
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

  // === Сброс поиска ===
  const handleResetSearch = () => {
    setSearchEmail('');
    loadEmployees();
  };

  // === Открыть модалку создания ===
  const handleCreate = () => {
    setEditingEmployee(null);
    form.resetFields();
    setModalOpen(true);
  };

  // === Открыть модалку редактирования ===
  const handleEdit = (employee: Employee) => {
    setEditingEmployee(employee);
    form.setFieldsValue({
      firstName: employee.firstName,
      lastName: employee.lastName,
      email: employee.email,
      phoneNumber: employee.phoneNumber,
      department: employee.department,
      position: employee.position,
    });
    setModalOpen(true);
  };

  // === Сохранить (создать или обновить) ===
  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      setSaving(true);

      if (editingEmployee) {
        // Обновляем
        await employeesApi.update(editingEmployee.id, values);
        message.success('Сотрудник обновлён');
      } else {
        // Создаём
        await employeesApi.create(values as CreateEmployeeRequest);
        message.success('Сотрудник создан');
      }

      setModalOpen(false);
      form.resetFields();
      loadEmployees();
    } catch (err: any) {
      if (err.errorFields) return; // ошибки валидации формы — не показываем
      message.error(err.response?.data?.message || 'Ошибка сохранения');
    } finally {
      setSaving(false);
    }
  };

  // === Удалить сотрудника ===
  const handleDelete = async (id: string) => {
    try {
      await employeesApi.delete(id);
      message.success('Сотрудник удалён');
      loadEmployees();
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Ошибка удаления');
    }
  };

  // === Колонки таблицы ===
  const columns = [
    { title: 'Имя', dataIndex: 'firstName', key: 'firstName' },
    { title: 'Фамилия', dataIndex: 'lastName', key: 'lastName' },
    { title: 'Email', dataIndex: 'email', key: 'email' },
    { title: 'Отдел', dataIndex: 'department', key: 'department' },
    {
      title: 'Должность',
      dataIndex: 'position',
      key: 'position',
      render: (v: string | null) => v || '—',
    },
    {
      title: 'Статус',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
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

  // === Рендер ===
  if (loading && employees.length === 0) {
    return <Spin size="large" style={{ display: 'block', marginTop: 100 }} />;
  }

  return (
      <div>
        <Title level={2} style={{ color: 'white' }}>
          Управление работниками
        </Title>

        {/* Панель поиска и кнопок */}
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

        {/* Ошибка */}
        {error && (
            <Alert
                message={error}
                type="error"
                showIcon
                style={{ marginBottom: 16 }}
            />
        )}

        {/* Таблица */}
        <Table
            dataSource={employees}
            columns={columns}
            rowKey="id"
            loading={loading}
            pagination={{ pageSize: 10 }}
        />

        {/* Модалка создания/редактирования */}
        <Modal
            title={editingEmployee ? 'Редактирование сотрудника' : 'Новый сотрудник'}
            open={modalOpen}
            onOk={handleSave}
            onCancel={() => setModalOpen(false)}
            confirmLoading={saving}
            okText="Сохранить"
            cancelText="Отмена"
            width={600}
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

            <Form.Item name="department" label="Отдел">
              <Input />
            </Form.Item>

            <Form.Item name="position" label="Должность">
              <Input />
            </Form.Item>
          </Form>
        </Modal>
      </div>
  );
};

export default EmployeesList;