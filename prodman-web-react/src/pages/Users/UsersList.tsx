import { useEffect, useMemo, useState } from 'react';
import {
  Typography, Card, Table, Button, Space, Tag, Modal, Form, Input, Select,
  message, Popconfirm, DatePicker, Alert, Empty, Tooltip,
} from 'antd';
import {
  PlusOutlined, DeleteOutlined, ClockCircleOutlined, UserOutlined,
} from '@ant-design/icons';
import dayjs, { Dayjs } from 'dayjs';
import { useAuthStore } from '../../store/auth.store';
import { usersApi } from '../../api/users.api';
import type { AppUser } from '../../api/users.api';
import { departmentsApi } from '../../api/departments.api';
import type { Department } from '../../api/departments.api';
import { Role } from '../../types/auth.types';

const { Title, Text } = Typography;

// Уровни ролей — должны совпадать с Role.level() на бэкенде.
const ROLE_LEVEL: Record<Role, number> = {
  PLATFORM_ADMIN: 0,
  ADMIN: 100,
  HEAD_OF_DEPARTMENT: 80,
  TENANT_VISITOR: 60,
  DEPT_VISITOR: 40,
  VISITOR: 20,
  ANALYST_1: 0,
  ANALYST_2: 0,
};

const ASSIGNABLE_ROLES: Role[] = [
  'ADMIN',
  'HEAD_OF_DEPARTMENT',
  'TENANT_VISITOR',
  'DEPT_VISITOR',
  'VISITOR',
];

const ROLE_LABELS: Record<Role, string> = {
  PLATFORM_ADMIN: 'Админ платформы',
  ADMIN: 'Админ тенанта',
  HEAD_OF_DEPARTMENT: 'Глава департамента',
  TENANT_VISITOR: 'Визитор тенанта',
  DEPT_VISITOR: 'Визитор департамента',
  VISITOR: 'Демо-доступ',
  ANALYST_1: 'Аналитик 1',
  ANALYST_2: 'Аналитик 2',
};

const ROLE_COLORS: Record<Role, string> = {
  PLATFORM_ADMIN: 'magenta',
  ADMIN: 'red',
  HEAD_OF_DEPARTMENT: 'orange',
  TENANT_VISITOR: 'blue',
  DEPT_VISITOR: 'cyan',
  VISITOR: 'default',
  ANALYST_1: 'purple',
  ANALYST_2: 'purple',
};

const UsersList = () => {
  const { user } = useAuthStore();
  const currentRole = user?.role;
  const currentDepartmentId = user?.departmentId ?? null;

  const [users, setUsers] = useState<AppUser[]>([]);
  const [loading, setLoading] = useState(false);
  const [departments, setDepartments] = useState<Department[]>([]);

  const [createOpen, setCreateOpen] = useState(false);
  const [createForm] = Form.useForm();

  const [tempRoleUser, setTempRoleUser] = useState<AppUser | null>(null);
  const [tempRoleForm] = Form.useForm();

  const isPlatformAdmin = currentRole === 'PLATFORM_ADMIN';
  const isHead = currentRole === 'HEAD_OF_DEPARTMENT';
  const currentLevel = currentRole ? ROLE_LEVEL[currentRole] : 0;
  const canManageRoles = currentLevel > ROLE_LEVEL.VISITOR;

  const load = async () => {
    setLoading(true);
    try {
      const [usersRes, depsRes] = await Promise.all([
        usersApi.getAll(0, 500),
        departmentsApi.getAll(0, 500),
      ]);
      setUsers(usersRes.content ?? []);
      setDepartments(depsRes.content ?? []);
    } catch {
      message.error('Ошибка загрузки пользователей');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!isPlatformAdmin) load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isPlatformAdmin]);

  const departmentName = (id: string | null) => {
    if (!id) return '—';
    return departments.find((d) => d.id === id)?.name ?? '—';
  };

  const creatorLabel = (u: AppUser): string => {
    if (!u.createdBy) return 'система';
    if (u.createdByRole === 'PLATFORM_ADMIN') return 'платформа';
    const creator = users.find((x) => x.id === u.createdBy);
    return creator ? creator.username : '—';
  };

  const canDeleteUser = (u: AppUser): boolean => {
    if (!user) return false;
    if (u.id === user.id) return false;                         // не себя
    if (!u.createdBy) return false;                             // сидовый
    if (u.createdByRole === 'PLATFORM_ADMIN') return false;     // создан платформой
    if (u.createdBy !== user.id) return false;                  // не мой
    if (ROLE_LEVEL[u.role] >= currentLevel) return false;       // роль не ниже моей
    return true;
  };

  const filteredUsers = useMemo(() => {
    if (isHead) {
      if (!currentDepartmentId) return [];
      return users.filter((u) => u.departmentId === currentDepartmentId);
    }
    return users;
  }, [users, isHead, currentDepartmentId]);

  const availableDepartments = useMemo(() => {
    if (isHead) {
      return departments.filter((d) => d.id === currentDepartmentId);
    }
    return departments;
  }, [departments, isHead, currentDepartmentId]);

  const creatableRoles = useMemo<Role[]>(() => {
    return ASSIGNABLE_ROLES.filter((r) => ROLE_LEVEL[r] <= currentLevel);
  }, [currentLevel]);

  if (isPlatformAdmin) {
    return (
        <div>
          <Title level={3} style={{ color: 'white', marginTop: 0, marginBottom: 24 }}>
            Тенанты платформы
          </Title>
          <Card style={{ background: '#2d2d3f', border: 'none' }}>
            <Empty description={<span style={{ color: '#aaa' }}>Управление тенантами появится позже</span>} />
          </Card>
        </div>
    );
  }

  const handleCreate = async () => {
    try {
      const values = await createForm.validateFields();
      await usersApi.create({
        username: values.username,
        email: values.email,
        password: values.password,
        role: values.role,
        departmentId: values.departmentId || null,
      });
      message.success('Пользователь создан');
      setCreateOpen(false);
      createForm.resetFields();
      load();
    } catch (err: any) {
      if (err.errorFields) return;
      message.error(err.response?.data?.message || 'Ошибка создания');
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await usersApi.delete(id);
      message.success('Пользователь удалён');
      load();
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Ошибка удаления');
    }
  };

  const handleOpenTempRole = (u: AppUser) => {
    setTempRoleUser(u);
    tempRoleForm.resetFields();
  };

  const handleSaveTempRole = async () => {
    if (!tempRoleUser) return;
    try {
      const values = await tempRoleForm.validateFields();
      const validUntil = values.validUntil
          ? (values.validUntil as Dayjs).endOf('day').toISOString()
          : null;
      await usersApi.assignTempRole(tempRoleUser.id, values.role, validUntil);
      message.success('Временная роль назначена');
      setTempRoleUser(null);
      load();
    } catch (err: any) {
      if (err.errorFields) return;
      message.error(err.response?.data?.message || 'Ошибка');
    }
  };

  const handleRevokeTempRole = async (userId: string, role: Role) => {
    try {
      await usersApi.revokeTempRole(userId, role);
      message.success('Временная роль отозвана');
      load();
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Ошибка');
    }
  };

  const columns = [
    {
      title: 'Пользователь',
      dataIndex: 'username',
      key: 'username',
      render: (v: string) => (
          <Space><UserOutlined />{v}</Space>
      ),
    },
    { title: 'Email', dataIndex: 'email', key: 'email' },
    {
      title: 'Основная роль',
      dataIndex: 'role',
      key: 'role',
      render: (role: Role) => <Tag color={ROLE_COLORS[role]}>{ROLE_LABELS[role]}</Tag>,
    },
    {
      title: 'Временные роли',
      key: 'tempRoles',
      render: (_: unknown, u: AppUser) => {
        if (!u.tempRoles || u.tempRoles.length === 0) return <Text type="secondary">—</Text>;
        return (
            <Space wrap>
              {u.tempRoles.map((tr) => (
                  <Tag
                      key={tr.id}
                      color={ROLE_COLORS[tr.role]}
                      closable={canManageRoles && ROLE_LEVEL[tr.role] < currentLevel}
                      onClose={(e) => {
                        e.preventDefault();
                        handleRevokeTempRole(u.id, tr.role);
                      }}
                  >
                    {ROLE_LABELS[tr.role]}
                    {tr.validUntil ? ` до ${dayjs(tr.validUntil).format('DD.MM.YYYY')}` : ' (бессрочно)'}
                  </Tag>
              ))}
            </Space>
        );
      },
    },
    {
      title: 'Департамент',
      dataIndex: 'departmentId',
      key: 'departmentId',
      render: (v: string | null) => departmentName(v),
    },
    {
      title: 'Создатель',
      key: 'createdBy',
      render: (_: unknown, u: AppUser) => {
        const label = creatorLabel(u);
        const tag = u.createdByRole === 'PLATFORM_ADMIN'
            ? <Tag color="magenta">{label}</Tag>
            : u.createdBy
                ? <Tag color={u.createdByRole ? ROLE_COLORS[u.createdByRole] : 'default'}>{label}</Tag>
                : <Tag>{label}</Tag>;
        return tag;
      },
    },
    {
      title: 'Действия',
      key: 'actions',
      render: (_: unknown, u: AppUser) => (
          <Space>
            {canManageRoles && creatableRoles.length > 0 && (
                <Button
                    size="small"
                    icon={<ClockCircleOutlined />}
                    onClick={() => handleOpenTempRole(u)}
                >
                  Временная роль
                </Button>
            )}
            {canDeleteUser(u) && (
                <Popconfirm
                    title="Удалить пользователя?"
                    onConfirm={() => handleDelete(u.id)}
                    okText="Да"
                    cancelText="Нет"
                >
                  <Button size="small" danger icon={<DeleteOutlined />} />
                </Popconfirm>
            )}
            {!canDeleteUser(u) && u.id !== user?.id && (
                <Tooltip title="Удалить может только создатель (или нельзя — системный/платформенный пользователь)">
                  <Button size="small" disabled icon={<DeleteOutlined />} />
                </Tooltip>
            )}
          </Space>
      ),
    },
  ];

  return (
      <div>
        <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }} wrap>
          <Title level={3} style={{ color: 'white', margin: 0 }}>
            Пользователи
            {isHead && (
                <Text style={{ color: '#aaa', fontSize: 14, marginLeft: 12 }}>
                  (только ваш департамент)
                </Text>
            )}
          </Title>
          {creatableRoles.length > 0 && (
              <Button
                  type="primary"
                  icon={<PlusOutlined />}
                  onClick={() => setCreateOpen(true)}
                  style={{ background: '#2E8B57', borderColor: '#2E8B57' }}
              >
                Создать пользователя
              </Button>
          )}
        </Space>

        <Card style={{ background: '#2d2d3f', border: 'none' }} styles={{ body: { padding: 0 } }}>
          <Table<AppUser>
              rowKey="id"
              dataSource={filteredUsers}
              columns={columns}
              loading={loading}
              pagination={{ pageSize: 20, showSizeChanger: true }}
          />
        </Card>

        <Modal
            title="Создать пользователя"
            open={createOpen}
            onOk={handleCreate}
            onCancel={() => { setCreateOpen(false); createForm.resetFields(); }}
            okText="Создать"
            cancelText="Отмена"
        >
          <Form form={createForm} layout="vertical">
            <Form.Item
                name="username"
                label="Имя пользователя"
                rules={[{ required: true, min: 3 }]}
            >
              <Input />
            </Form.Item>
            <Form.Item
                name="email"
                label="Email"
                rules={[{ required: true, type: 'email' }]}
            >
              <Input />
            </Form.Item>
            <Form.Item
                name="password"
                label="Пароль"
                rules={[{ required: true, min: 6 }]}
            >
              <Input.Password />
            </Form.Item>
            <Form.Item
                name="role"
                label="Роль"
                rules={[{ required: true }]}
            >
              <Select
                  options={creatableRoles.map((r) => ({
                    value: r,
                    label: ROLE_LABELS[r],
                  }))}
              />
            </Form.Item>
            <Form.Item
                noStyle
                shouldUpdate={(prev, curr) => prev.role !== curr.role}
            >
              {({ getFieldValue }) => {
                const r = getFieldValue('role') as Role | undefined;
                const needsDept = r === 'HEAD_OF_DEPARTMENT' || r === 'DEPT_VISITOR';
                if (!needsDept) return null;
                return (
                    <Form.Item
                        name="departmentId"
                        label="Департамент"
                        rules={[{ required: true, message: 'Укажите департамент' }]}
                    >
                      <Select
                          placeholder="Выберите департамент"
                          options={availableDepartments.map((d) => ({ value: d.id, label: d.name }))}
                      />
                    </Form.Item>
                );
              }}
            </Form.Item>
          </Form>
        </Modal>

        <Modal
            title={`Временная роль для ${tempRoleUser?.username ?? ''}`}
            open={!!tempRoleUser}
            onOk={handleSaveTempRole}
            onCancel={() => setTempRoleUser(null)}
            okText="Назначить"
            cancelText="Отмена"
        >
          <Alert
              type="info"
              showIcon
              style={{ marginBottom: 16 }}
              message="Временная роль действует до указанной даты. После — автоматически перестаёт учитываться при входе."
          />
          <Form form={tempRoleForm} layout="vertical">
            <Form.Item
                name="role"
                label="Роль"
                rules={[{ required: true }]}
            >
              <Select
                  options={creatableRoles.map((r) => ({
                    value: r,
                    label: ROLE_LABELS[r],
                  }))}
              />
            </Form.Item>
            <Form.Item
                name="validUntil"
                label="Действует до"
                rules={[{ required: true, message: 'Укажите дату' }]}
            >
              <DatePicker
                  style={{ width: '100%' }}
                  format="DD.MM.YYYY"
                  disabledDate={(d) => d && d.isBefore(dayjs(), 'day')}
              />
            </Form.Item>
          </Form>
        </Modal>
      </div>
  );
};

export default UsersList;