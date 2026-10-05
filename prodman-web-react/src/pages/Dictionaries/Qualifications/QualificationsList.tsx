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
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { qualificationsApi } from '../../../api/qualifications.api';
import type {
    Qualification,
    CreateQualificationRequest,
} from '../../../api/qualifications.api';
import { positionsApi } from '../../../api/positions.api';
import type { Position } from '../../../api/positions.api';

const { Title } = Typography;

const QualificationsList = () => {
    const [items, setItems] = useState<Qualification[]>([]);
    const [positions, setPositions] = useState<Position[]>([]);
    const [loading, setLoading] = useState(true);
    const [modalOpen, setModalOpen] = useState(false);
    const [editing, setEditing] = useState<Qualification | null>(null);
    const [saving, setSaving] = useState(false);
    const [form] = Form.useForm();

    const load = async () => {
        setLoading(true);
        try {
            const [q, p] = await Promise.all([
                qualificationsApi.getAll(0, 200),
                positionsApi.getAll(0, 200),
            ]);
            setItems(q.content ?? []);
            setPositions(p.content ?? []);
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

    const handleEdit = (q: Qualification) => {
        setEditing(q);
        form.setFieldsValue({
            positionId: q.positionId,
            code: q.code,
            name: q.name,
            description: q.description,
            isActive: q.isActive,
        });
        setModalOpen(true);
    };

    const handleSave = async () => {
        try {
            const values = await form.validateFields();
            setSaving(true);

            const payload: CreateQualificationRequest = {
                positionId: values.positionId,
                code: values.code,
                name: values.name,
                description: values.description,
                isActive: values.isActive,
            };

            if (editing) {
                await qualificationsApi.update(editing.id, payload);
                message.success('Квалификация обновлена');
            } else {
                await qualificationsApi.create(payload);
                message.success('Квалификация создана');
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
            await qualificationsApi.delete(id);
            message.success('Квалификация удалена');
            load();
        } catch (err: any) {
            message.error(err.response?.data?.message || 'Ошибка удаления');
        }
    };

    const columns = [
        { title: 'Код', dataIndex: 'code', key: 'code', width: 140 },
        { title: 'Название', dataIndex: 'name', key: 'name' },
        {
            title: 'Должность',
            dataIndex: 'positionName',
            key: 'positionName',
            render: (n: string | null) => (n ? <Tag color="blue">{n}</Tag> : '—'),
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
            render: (_: unknown, record: Qualification) => (
                <Space>
                    <Button icon={<EditOutlined />} size="small" onClick={() => handleEdit(record)} />
                    <Popconfirm
                        title="Удалить квалификацию?"
                        description="Если она назначена сотрудникам, удаление упадёт"
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
                    Квалификации
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

            <Table dataSource={items} columns={columns} rowKey="id" loading={loading} />

            <Modal
                title={editing ? 'Редактирование квалификации' : 'Новая квалификация'}
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
                        name="positionId"
                        label="Должность"
                        rules={[{ required: true, message: 'Выберите должность' }]}
                    >
                        <Select
                            placeholder="Выберите должность"
                            options={positions.map((p) => ({
                                value: p.id,
                                label: `${p.code} — ${p.name}${p.departmentName ? ` (${p.departmentName})` : ''}`,
                            }))}
                        />
                    </Form.Item>

                    <Form.Item
                        name="code"
                        label="Код"
                        rules={[{ required: true, message: 'Введите код' }]}
                    >
                        <Input placeholder="Например: Q-OPER-1" />
                    </Form.Item>

                    <Form.Item
                        name="name"
                        label="Название"
                        rules={[{ required: true, message: 'Введите название' }]}
                    >
                        <Input placeholder="Например: Оператор станка 1" />
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

export default QualificationsList;