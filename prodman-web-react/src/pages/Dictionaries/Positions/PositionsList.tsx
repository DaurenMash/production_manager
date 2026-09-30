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
            const data = await positionsApi.getAll();
            setPositions(Array.isArray(data) ? data : []);
        } catch (err: any) {
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
        form.setFieldsValue({ color: '#bbdefb' });
        setModalOpen(true);
    };

    const handleEdit = (position: Position) => {
        setEditing(position);
        form.setFieldsValue({
            name: position.name,
            color: position.color,
        });
        setModalOpen(true);
    };

    const handleSave = async () => {
        try {
            const values = await form.validateFields();
            setSaving(true);

            // ColorPicker возвращает объект Color, вытаскиваем hex
            const colorValue =
                typeof values.color === 'string'
                    ? values.color
                    : values.color?.toHexString?.() || '#bbdefb';

            const payload: CreatePositionRequest = {
                name: values.name,
                color: colorValue,
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
            render: (_: any, record: Position) => (
                <Space>
                    <Button
                        icon={<EditOutlined />}
                        size="small"
                        onClick={() => handleEdit(record)}
                    />
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

            <Table
                dataSource={positions}
                columns={columns}
                rowKey="id"
                loading={loading}
                pagination={{ pageSize: 10 }}
            />

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
                        name="name"
                        label="Название"
                        rules={[{ required: true, message: 'Введите название' }]}
                    >
                        <Input placeholder="Например: Печатник" />
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

export default PositionsList;