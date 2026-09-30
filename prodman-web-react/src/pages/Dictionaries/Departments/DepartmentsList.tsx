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
    ColorPicker,
    Tag,
} from 'antd';
import {
    PlusOutlined,
    EditOutlined,
    DeleteOutlined,
} from '@ant-design/icons';
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
            const data = await departmentsApi.getAll();
            setDepartments(Array.isArray(data) ? data : []);
        } catch (err: any) {
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
        form.setFieldsValue({ color: '#e1bee7' });
        setModalOpen(true);
    };

    const handleEdit = (department: Department) => {
        setEditing(department);
        form.setFieldsValue({
            name: department.name,
            color: department.color,
        });
        setModalOpen(true);
    };

    const handleSave = async () => {
        try {
            const values = await form.validateFields();
            setSaving(true);

            const colorValue =
                typeof values.color === 'string'
                    ? values.color
                    : values.color?.toHexString?.() || '#e1bee7';

            const payload: CreateDepartmentRequest = {
                name: values.name,
                color: colorValue,
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
        {
            title: 'Цвет',
            dataIndex: 'color',
            key: 'color',
            width: 100,
            render: (color: string) => (
                <Tag color={color} style={{ width: 40, height: 20, borderRadius: 4 }}>
                    &nbsp;
                </Tag>
            ),
        },
        { title: 'Название', dataIndex: 'name', key: 'name' },
        {
            title: 'Действия',
            key: 'actions',
            width: 120,
            render: (_: any, record: Department) => (
                <Space>
                    <Button
                        icon={<EditOutlined />}
                        size="small"
                        onClick={() => handleEdit(record)}
                    />
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

            <Table
                dataSource={departments}
                columns={columns}
                rowKey="id"
                loading={loading}
                pagination={{ pageSize: 10 }}
            />

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
                        name="name"
                        label="Название"
                        rules={[{ required: true, message: 'Введите название' }]}
                    >
                        <Input placeholder="Например: Печатный цех" />
                    </Form.Item>

                    <Form.Item
                        name="color"
                        label="Цвет"
                        rules={[{ required: true, message: 'Выберите цвет' }]}
                    >
                        <ColorPicker
                            showText
                            format="hex"
                            presets={[
                                {
                                    label: 'Светлые',
                                    colors: [
                                        '#ffffff',
                                        '#e0e0e0',
                                        '#bbdefb',
                                        '#c8e6c9',
                                        '#fff9c4',
                                        '#ffcc80',
                                        '#ffcdd2',
                                        '#e1bee7',
                                        '#b2dfdb',
                                        '#d7ccc8',
                                        '#d1c4e9',
                                        '#b2ebf2',
                                    ],
                                },
                            ]}
                        />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default DepartmentsList;