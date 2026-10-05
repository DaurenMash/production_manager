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
} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { departmentsApi } from '../../../api/departments.api';
import type { Department, CreateDepartmentRequest } from '../../../api/departments.api';

const { Title } = Typography;

const DepartmentsList = () => {
    const [departments, setDepartments] = useState<Department[]>([]);
    const [loading, setLoading] = useState(true);
    const [modalOpen, setModalOpen] = useState(false);
    const [editing, setEditing] = useState<Department | null>(null);
    const [saving, setSaving] = useState(false);
    const [form] = Form.useForm();

    const load = async () => {
        setLoading(true);
        try {
            const data = await departmentsApi.getAll(0, 200);
            setDepartments(data.content ?? []);
        } catch {
            message.error('Ошибка загрузки отделов');
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

    const handleEdit = (department: Department) => {
        setEditing(department);
        form.setFieldsValue({
            code: department.code,
            name: department.name,
            description: department.description,
            isActive: department.isActive,
        });
        setModalOpen(true);
    };

    const handleSave = async () => {
        try {
            const values = await form.validateFields();
            setSaving(true);

            const payload: CreateDepartmentRequest = {
                code: values.code,
                name: values.name,
                description: values.description,
                isActive: values.isActive,
            };

            if (editing) {
                await departmentsApi.update(editing.id, payload);
                message.success('Отдел обновлён');
            } else {
                await departmentsApi.create(payload);
                message.success('Отдел создан');
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
            await departmentsApi.delete(id);
            message.success('Отдел удалён');
            load();
        } catch (err: any) {
            message.error(err.response?.data?.message || 'Ошибка удаления');
        }
    };

    const columns = [
        { title: 'Код', dataIndex: 'code', key: 'code', width: 120 },
        { title: 'Название', dataIndex: 'name', key: 'name' },
        { title: 'Описание', dataIndex: 'description', key: 'description' },
        {
            title: 'Активен',
            dataIndex: 'isActive',
            key: 'isActive',
            width: 120,
            render: (v: boolean) => (v ? <Tag color="green">Да</Tag> : <Tag>Нет</Tag>),
        },
        {
            title: 'Действия',
            key: 'actions',
            width: 120,
            render: (_: unknown, record: Department) => (
                <Space>
                    <Button icon={<EditOutlined />} size="small" onClick={() => handleEdit(record)} />
                    <Popconfirm
                        title="Удалить отдел?"
                        description="Если на него ссылаются сотрудники, удаление упадёт"
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
                    Отделы
                </Title>
                <Button
                    type="primary"
                    icon={<PlusOutlined />}
                    onClick={handleCreate}
                    style={{ background: '#2ecc71', borderColor: '#2ecc71' }}
                >
                    Создать
                </Button>
            </Space>

            <Table dataSource={departments} columns={columns} rowKey="id" loading={loading} />

            <Modal
                title={editing ? 'Редактирование отдела' : 'Новый отдел'}
                open={modalOpen}
                onOk={handleSave}
                onCancel={() => setModalOpen(false)}
                confirmLoading={saving}
                okText="Сохранить"
                cancelText="Отмена"
                width={500}
            >
                <Form form={form} layout="vertical">
                    <Form.Item
                        name="code"
                        label="Код"
                        rules={[{ required: true, message: 'Введите код' }]}
                    >
                        <Input placeholder="Например: SHOP-1" />
                    </Form.Item>

                    <Form.Item
                        name="name"
                        label="Название"
                        rules={[{ required: true, message: 'Введите название' }]}
                    >
                        <Input placeholder="Например: Печатный цех" />
                    </Form.Item>

                    <Form.Item name="description" label="Описание">
                        <Input.TextArea rows={3} />
                    </Form.Item>

                    <Form.Item name="isActive" label="Активен" valuePropName="checked">
                        <Switch />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default DepartmentsList;