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
import { positionsApi } from '../../../api/positions.api';
import type { Position, CreatePositionRequest } from '../../../api/positions.api';

const { Title } = Typography;

const PositionsList = () => {
    const [positions, setPositions] = useState<Position[]>([]);
    const [loading, setLoading] = useState(true);
    const [modalOpen, setModalOpen] = useState(false);
    const [editing, setEditing] = useState<Position | null>(null);
    const [saving, setSaving] = useState(false);
    const [form] = Form.useForm();

    const load = async () => {
        setLoading(true);
        try {
            const data = await positionsApi.getAll(0, 200);
            setPositions(data.content ?? []);
        } catch {
            message.error('Ошибка загрузки должностей');
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

    const handleEdit = (position: Position) => {
        setEditing(position);
        form.setFieldsValue({
            code: position.code,
            name: position.name,
            description: position.description,
            isActive: position.isActive,
        });
        setModalOpen(true);
    };

    const handleSave = async () => {
        try {
            const values = await form.validateFields();
            setSaving(true);

            const payload: CreatePositionRequest = {
                code: values.code,
                name: values.name,
                description: values.description,
                isActive: values.isActive,
            };

            if (editing) {
                await positionsApi.update(editing.id, payload);
                message.success('Должность обновлена');
            } else {
                await positionsApi.create(payload);
                message.success('Должность создана');
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
            await positionsApi.delete(id);
            message.success('Должность удалена');
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
            title: 'Активна',
            dataIndex: 'isActive',
            key: 'isActive',
            width: 120,
            render: (v: boolean) => (v ? <Tag color="green">Да</Tag> : <Tag>Нет</Tag>),
        },
        {
            title: 'Действия',
            key: 'actions',
            width: 120,
            render: (_: unknown, record: Position) => (
                <Space>
                    <Button icon={<EditOutlined />} size="small" onClick={() => handleEdit(record)} />
                    <Popconfirm
                        title="Удалить должность?"
                        description="Если на неё ссылаются сотрудники, удаление упадёт"
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
                    Должности
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

            <Table dataSource={positions} columns={columns} rowKey="id" loading={loading} />

            <Modal
                title={editing ? 'Редактирование должности' : 'Новая должность'}
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
                        <Input placeholder="Например: POS-1" />
                    </Form.Item>

                    <Form.Item
                        name="name"
                        label="Название"
                        rules={[{ required: true, message: 'Введите название' }]}
                    >
                        <Input placeholder="Например: Печатник" />
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

export default PositionsList;