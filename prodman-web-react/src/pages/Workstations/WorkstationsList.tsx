import { useEffect, useState } from 'react';
import {
  Table, Button, Space, Modal, Form, Input, Select, Switch, message, Popconfirm, Tag,
} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';

import { workstationsApi } from '../../api/workstations.api';
import type { Workstation } from '../../api/workstations.api';
import { departmentsApi } from '../../api/departments.api';
import type { Department } from '../../api/departments.api';
import { qualificationsApi } from '../../api/qualifications.api';
import type { Qualification } from '../../api/qualifications.api';

const WorkstationsList = () => {
  const [items, setItems] = useState<Workstation[]>([]);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [qualifications, setQualifications] = useState<Qualification[]>([]);
  const [loading, setLoading] = useState(false);

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Workstation | null>(null);
  const [form] = Form.useForm();

  const load = async () => {
    setLoading(true);
    try {
      const [ws, dep, qs] = await Promise.all([
        workstationsApi.getAll(0, 500),
        departmentsApi.getAll(0, 500),
        qualificationsApi.getAll(0, 500),
      ]);
      setItems(ws.content ?? []);
      setDepartments(dep.content ?? []);
      setQualifications(qs.content ?? []);
    } catch {
      message.error('Ошибка загрузки данных');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    form.setFieldsValue({ isActive: true, qualificationIds: [] });
    setModalOpen(true);
  };

  const openEdit = (w: Workstation) => {
    setEditing(w);
    form.setFieldsValue({
      departmentId: w.departmentId,
      code: w.code,
      name: w.name,
      description: w.description,
      isActive: w.isActive,
      qualificationIds: w.qualificationIds ?? [],
    });
    setModalOpen(true);
  };

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      const { qualificationIds, ...wsData } = values;

      let saved: Workstation;
      if (editing) {
        saved = await workstationsApi.update(editing.id, wsData);
      } else {
        saved = await workstationsApi.create(wsData);
      }

      await workstationsApi.setQualifications(saved.id, qualificationIds ?? []);

      message.success(editing ? 'Станок обновлён' : 'Станок создан');
      setModalOpen(false);
      load();
    } catch (err: any) {
      if (err.errorFields) return;
      message.error(err.response?.data?.message || 'Ошибка сохранения');
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await workstationsApi.delete(id);
      message.success('Удалено');
      load();
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Ошибка удаления');
    }
  };

  const departmentName = (id: string) =>
      departments.find((d) => d.id === id)?.name ?? id.slice(0, 8);

  const qualificationName = (id: string) => {
    const q = qualifications.find((x) => x.id === id);
    return q ? `${q.code} — ${q.name}` : id.slice(0, 8);
  };

  return (
      <div>
        <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }}>
          <span style={{ color: 'white', fontSize: 20, fontWeight: 600 }}>Рабочие места</span>
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            Добавить
          </Button>
        </Space>

        <Table<Workstation>
            rowKey="id"
            loading={loading}
            dataSource={items}
            pagination={{ pageSize: 20 }}
            columns={[
              { title: 'Код', dataIndex: 'code', width: 120 },
              { title: 'Название', dataIndex: 'name' },
              {
                title: 'Отдел',
                dataIndex: 'departmentId',
                render: (v: string) => departmentName(v),
                width: 200,
              },
              {
                title: 'Квалификации',
                dataIndex: 'qualificationIds',
                render: (ids: string[]) =>
                    !ids || ids.length === 0
                        ? <span style={{ color: '#888' }}>—</span>
                        : <Space wrap size={4}>
                          {ids.map((id) => (
                              <Tag key={id} color="blue">{qualificationName(id)}</Tag>
                          ))}
                        </Space>,
              },
              {
                title: 'Активен',
                dataIndex: 'isActive',
                width: 90,
                render: (v: boolean) => (v ? '✅' : '❌'),
              },
              {
                title: '',
                width: 120,
                render: (_: any, w: Workstation) => (
                    <Space>
                      <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(w)} />
                      <Popconfirm
                          title="Удалить станок?"
                          onConfirm={() => handleDelete(w.id)}
                          okText="Да"
                          cancelText="Нет"
                      >
                        <Button size="small" danger icon={<DeleteOutlined />} />
                      </Popconfirm>
                    </Space>
                ),
              },
            ]}
        />

        <Modal
            title={editing ? 'Редактировать станок' : 'Новый станок'}
            open={modalOpen}
            onOk={handleSave}
            onCancel={() => setModalOpen(false)}
            okText="Сохранить"
            cancelText="Отмена"
            width={560}
        >
          <Form form={form} layout="vertical">
            <Form.Item
                name="departmentId"
                label="Отдел"
                rules={[{ required: true, message: 'Выберите отдел' }]}
            >
              <Select
                  options={departments.map((d) => ({
                    value: d.id,
                    label: `${d.code} — ${d.name}`,
                  }))}
              />
            </Form.Item>
            <Form.Item
                name="code"
                label="Код"
                rules={[{ required: true, message: 'Введите код' }]}
            >
              <Input />
            </Form.Item>
            <Form.Item
                name="name"
                label="Название"
                rules={[{ required: true, message: 'Введите название' }]}
            >
              <Input />
            </Form.Item>
            <Form.Item name="description" label="Описание">
              <Input.TextArea rows={2} />
            </Form.Item>
            <Form.Item
                name="qualificationIds"
                label="Квалификации, требуемые на станке"
            >
              <Select
                  mode="multiple"
                  allowClear
                  placeholder="Выберите одну или несколько"
                  optionFilterProp="label"
                  options={qualifications.map((q) => ({
                    value: q.id,
                    label: `${q.code} — ${q.name}`,
                  }))}
              />
            </Form.Item>
            <Form.Item name="isActive" label="Активен" valuePropName="checked">
              <Switch />
            </Form.Item>
          </Form>
        </Modal>
      </div>
  );
};

export default WorkstationsList;