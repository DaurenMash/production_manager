import { useEffect, useState } from 'react';
import {
  Typography,
  Table,
  Button,
  Space,
  Modal,
  Form,
  Input,
  Popconfirm,
  message,
  Switch,
  Tag,
  Select,
} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons';
import { workstationsApi } from '../../api/workstations.api';
import type { Workstation, CreateWorkstationRequest } from '../../api/workstations.api';
import { departmentsApi } from '../../api/departments.api';
import type { Department } from '../../api/departments.api';
import { qualificationsApi } from '../../api/qualifications.api';
import type { Qualification } from '../../api/qualifications.api';

const { Title } = Typography;

const WorkstationsList = () => {
  const [workstations, setWorkstations] = useState<Workstation[]>([]);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [qualifications, setQualifications] = useState<Qualification[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Workstation | null>(null);
  const [saving, setSaving] = useState(false);
  const [form] = Form.useForm();

  const load = async () => {
    setLoading(true);
    try {
      const [ws, dep, quals] = await Promise.all([
        workstationsApi.getAll(0, 200),
        departmentsApi.getAll(0, 200),
        qualificationsApi.getAll(0, 500),
      ]);
      setWorkstations(ws.content ?? []);
      setDepartments(dep.content ?? []);
      setQualifications(quals.content ?? []);
    } catch {
      message.error('Ошибка загрузки данных');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const handleCreate = () => {
    setEditing(null);
    form.resetFields();
    form.setFieldsValue({ isActive: true });
    setModalOpen(true);
  };

  const handleEdit = (w: Workstation) => {
    setEditing(w);
    form.setFieldsValue({
      departmentId: w.departmentId,
      requiredQualificationId: w.requiredQualificationId ?? undefined,
      code: w.code,
      name: w.name,
      description: w.description,
      isActive: w.isActive,
    });
    setModalOpen(true);
  };

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      setSaving(true);

      const payload: CreateWorkstationRequest = {
        departmentId: values.departmentId,
        requiredQualificationId: values.requiredQualificationId || undefined,
        code: values.code,
        name: values.name,
        description: values.description,
        isActive: values.isActive,
      };

      if (editing) {
        await workstationsApi.update(editing.id, payload);
        message.success('Рабочая станция обновлена');
      } else {
        await workstationsApi.create(payload);
        message.success('Рабочая станция создана');
      }

      setModalOpen(false);
      form.resetFields();
      load();
    } catch (err: any) {
      if (err.errorFields) return;
      message.error(err.response?.data?.message || 'Ошибка сохранения');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await workstationsApi.delete(id);
      message.success('Рабочая станция удалена');
      load();
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Ошибка удаления');
    }
  };

  const getDepartmentName = (id: string | null): string | null => {
    if (!id) return null;
    return departments.find((d) => d.id === id)?.name ?? null;
  };

  const getQualificationName = (id: string | null): string | null => {
    if (!id) return null;
    return qualifications.find((q) => q.id === id)?.name ?? null;
  };

  const columns = [
    { title: 'Код', dataIndex: 'code', key: 'code', width: 120 },
    { title: 'Название', dataIndex: 'name', key: 'name' },
    {
      title: 'Отдел',
      dataIndex: 'departmentId',
      key: 'departmentId',
      render: (id: string | null) => {
        const name = getDepartmentName(id);
        return name ? <Tag color="blue">{name}</Tag> : '—';
      },
    },
    {
      title: 'Требуемая квалификация',
      dataIndex: 'requiredQualificationId',
      key: 'requiredQualificationId',
      render: (id: string | null) => {
        const name = getQualificationName(id);
        return name ? <Tag color="green">{name}</Tag> : '—';
      },
    },
    { title: 'Описание', dataIndex: 'description', key: 'description' },
    {
      title: 'Активна',
      dataIndex: 'isActive',
      key: 'isActive',
      width: 100,
      render: (v: boolean) => (v ? <Tag color="green">Да</Tag> : <Tag>Нет</Tag>),
    },
    {
      title: 'Действия',
      key: 'actions',
      width: 120,
      render: (_: unknown, record: Workstation) => (
          <Space>
            <Button icon={<EditOutlined />} size="small" onClick={() => handleEdit(record)} />
            <Popconfirm
                title="Удалить рабочую станцию?"
                description="Если она используется в назначениях, удаление упадёт"
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

  return (
      <div>
        <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }}>
          <Title level={3} style={{ color: 'white', margin: 0 }}>
            Рабочие станции
          </Title>
          <Space>
            <Button onClick={load} icon={<ReloadOutlined />}>
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
        </Space>

        <Table dataSource={workstations} columns={columns} rowKey="id" loading={loading} />

        <Modal
            title={editing ? 'Редактирование рабочей станции' : 'Новая рабочая станция'}
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
                name="code"
                label="Код"
                rules={[{ required: true, message: 'Введите код' }]}
            >
              <Input placeholder="Например: WS-001" />
            </Form.Item>

            <Form.Item
                name="name"
                label="Название"
                rules={[{ required: true, message: 'Введите название' }]}
            >
              <Input placeholder="Например: Печатный станок №1" />
            </Form.Item>

            <Form.Item
                name="departmentId"
                label="Отдел"
                rules={[{ required: true, message: 'Выберите отдел' }]}
            >
              <Select
                  placeholder="Выберите отдел"
                  options={departments.map((d) => ({ value: d.id, label: `${d.code} — ${d.name}` }))}
              />
            </Form.Item>

            <Form.Item
                name="requiredQualificationId"
                label="Требуемая квалификация"
                tooltip="Оставьте пустым, если станок не требует специальной квалификации"
            >
              <Select
                  placeholder="Выберите квалификацию (опционально)"
                  allowClear
                  showSearch
                  optionFilterProp="label"
                  options={qualifications.map((q) => ({
                    value: q.id,
                    label: `${q.code} — ${q.name}`,
                  }))}
              />
            </Form.Item>

            <Form.Item name="description" label="Описание">
              <Input.TextArea rows={3} />
            </Form.Item>

            <Form.Item name="isActive" label="Активна" valuePropName="checked">
              <Switch />
            </Form.Item>
          </Form>
        </Modal>
      </div>
  );
};

export default WorkstationsList;