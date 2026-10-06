import { useEffect, useState } from 'react';
import {
    Typography, Table, Button, Space, Modal, Form, Input, Popconfirm, message,
    Switch, Tag, TimePicker, InputNumber,
} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { shiftPatternsApi } from '../../api/shiftPatterns.api';
import type { ShiftPattern, CreateShiftPatternRequest } from '../../api/shiftPatterns.api';

const { Title } = Typography;

const ShiftPatternsList = () => {
    const [items, setItems] = useState<ShiftPattern[]>([]);
    const [loading, setLoading] = useState(true);
    const [modalOpen, setModalOpen] = useState(false);
    const [editing, setEditing] = useState<ShiftPattern | null>(null);
    const [saving, setSaving] = useState(false);
    const [form] = Form.useForm();

    const load = async () => {
        setLoading(true);
        try {
            const data = await shiftPatternsApi.getAll(0, 200);
            setItems(data.content ?? []);
        } catch {
            message.error('Ошибка загрузки шаблонов смен');
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
        form.setFieldsValue({ coefficient: 1.0, nightHours: 0, isActive: true });
        setModalOpen(true);
    };

    const handleEdit = (p: ShiftPattern) => {
        setEditing(p);
        form.setFieldsValue({
            code: p.code,
            name: p.name,
            startTime: dayjs(p.startTime, 'HH:mm:ss'),
            endTime: dayjs(p.endTime, 'HH:mm:ss'),
            totalHours: p.totalHours,
            nightHours: p.nightHours,
            coefficient: p.coefficient,
            isActive: p.isActive,
        });
        setModalOpen(true);
    };

    const handleSave = async () => {
        try {
            const values = await form.validateFields();
            setSaving(true);

            const payload: CreateShiftPatternRequest = {
                code: values.code,
                name: values.name,
                startTime: values.startTime.format('HH:mm:ss'),
                endTime: values.endTime.format('HH:mm:ss'),
                totalHours: values.totalHours,
                nightHours: values.nightHours ?? 0,
                coefficient: values.coefficient ?? 1.0,
                isActive: values.isActive,
            };

            if (editing) {
                await shiftPatternsApi.update(editing.id, payload);
                message.success('Шаблон обновлён');
            } else {
                await shiftPatternsApi.create(payload);
                message.success('Шаблон создан');
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
            await shiftPatternsApi.delete(id);
            message.success('Шаблон удалён');
            load();
        } catch (err: any) {
            message.error(err.response?.data?.message || 'Ошибка удаления');
        }
    };

    const columns = [
        { title: 'Код', dataIndex: 'code', key: 'code', width: 120 },
        { title: 'Название', dataIndex: 'name', key: 'name' },
        {
            title: 'Время',
            key: 'time',
            render: (_: unknown, r: ShiftPattern) =>
                `${r.startTime.slice(0, 5)} — ${r.endTime.slice(0, 5)}${r.crossesMidnight ? ' (+1)' : ''}`,
        },
        {
            title: 'Часы',
            key: 'hours',
            render: (_: unknown, r: ShiftPattern) => (
                <span>
          {r.totalHours}
                    {Number(r.nightHours) > 0 && (
                        <Tag color="purple" style={{ marginLeft: 8 }}>
                            ночь: {r.nightHours}
                        </Tag>
                    )}
        </span>
            ),
        },
        {
            title: 'Коэф.',
            dataIndex: 'coefficient',
            key: 'coefficient',
            width: 80,
        },
        {
            title: 'Активен',
            dataIndex: 'isActive',
            key: 'isActive',
            width: 100,
            render: (v: boolean) => (v ? <Tag color="green">Да</Tag> : <Tag>Нет</Tag>),
        },
        {
            title: 'Действия',
            key: 'actions',
            width: 120,
            render: (_: unknown, record: ShiftPattern) => (
                <Space>
                    <Button icon={<EditOutlined />} size="small" onClick={() => handleEdit(record)} />
                    <Popconfirm
                        title="Удалить шаблон?"
                        description="Если на него ссылаются назначения, удаление упадёт"
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
                    Шаблоны смен
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

            <Table dataSource={items} columns={columns} rowKey="id" loading={loading} />

            <Modal
                title={editing ? 'Редактирование шаблона' : 'Новый шаблон смены'}
                open={modalOpen}
                onOk={handleSave}
                onCancel={() => setModalOpen(false)}
                confirmLoading={saving}
                okText="Сохранить"
                cancelText="Отмена"
                width={520}
            >
                <Form form={form} layout="vertical">
                    <Form.Item
                        name="code"
                        label="Код"
                        rules={[{ required: true, message: 'Введите код' }]}
                    >
                        <Input placeholder="Например: DAY-12" />
                    </Form.Item>

                    <Form.Item
                        name="name"
                        label="Название"
                        rules={[{ required: true, message: 'Введите название' }]}
                    >
                        <Input placeholder="Например: Дневная 12ч" />
                    </Form.Item>

                    <Form.Item
                        name="startTime"
                        label="Начало"
                        rules={[{ required: true, message: 'Укажите время начала' }]}
                    >
                        <TimePicker format="HH:mm" style={{ width: '100%' }} />
                    </Form.Item>

                    <Form.Item
                        name="endTime"
                        label="Конец"
                        rules={[{ required: true, message: 'Укажите время окончания' }]}
                    >
                        <TimePicker format="HH:mm" style={{ width: '100%' }} />
                    </Form.Item>

                    <Form.Item
                        name="totalHours"
                        label="Всего часов"
                        tooltip="Например, 11.5 (11 часов 30 минут)"
                        rules={[{ required: true, message: 'Укажите общее число часов' }]}
                    >
                        <InputNumber min={0} max={24} step={0.5} style={{ width: '100%' }} />
                    </Form.Item>

                    <Form.Item
                        name="nightHours"
                        label="Из них ночных"
                        tooltip="Например, 7.5 (7 часов 30 минут). 0 — если смена дневная."
                    >
                        <InputNumber min={0} max={24} step={0.5} style={{ width: '100%' }} />
                    </Form.Item>

                    <Form.Item
                        name="coefficient"
                        label="Коэффициент"
                        tooltip="Множитель оплаты. Пока 1.0 для всех."
                    >
                        <InputNumber min={0.1} max={5} step={0.1} style={{ width: '100%' }} />
                    </Form.Item>

                    <Form.Item name="isActive" label="Активен" valuePropName="checked">
                        <Switch />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default ShiftPatternsList;