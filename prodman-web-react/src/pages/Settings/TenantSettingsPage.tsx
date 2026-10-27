import { useEffect, useState } from 'react';
import { Typography, Form, Input, InputNumber, Button, message, Card, Space, Select } from 'antd';
import { tenantSettingsApi } from '../../api/tenantSettings.api';
import type { TenantSettings, UpdateTenantSettingsRequest } from '../../api/tenantSettings.api';

const { Title, Text } = Typography;

const TenantSettingsPage = () => {
    const [form] = Form.useForm<UpdateTenantSettingsRequest>();
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);

    const load = async () => {
        setLoading(true);
        try {
            const data: TenantSettings = await tenantSettingsApi.get();
            form.setFieldsValue({
                monthCrossingMode: data.monthCrossingMode,
                baseCoefficientName: data.baseCoefficientName,
                baseCoefficientValue: Number(data.baseCoefficientValue),
                weekendCoefficientName: data.weekendCoefficientName,
                weekendCoefficientValue: Number(data.weekendCoefficientValue),
                nightCoefficientName: data.nightCoefficientName,
                nightCoefficientValue: Number(data.nightCoefficientValue),
            });
        } catch {
            message.error('Ошибка загрузки настроек');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        load();
    }, []);

    const handleSave = async () => {
        try {
            const values = await form.validateFields();
            setSaving(true);
            await tenantSettingsApi.update(values);
            message.success('Настройки сохранены');
        } catch (err: any) {
            if (err.errorFields) return;
            message.error(err.response?.data?.message || 'Ошибка сохранения');
        } finally {
            setSaving(false);
        }
    };

    return (
        <div style={{ maxWidth: 720 }}>
            <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }}>
                <Title level={3} style={{ color: 'white', margin: 0 }}>
                    Настройки тенанта
                </Title>
                <Button type="primary" loading={saving} onClick={handleSave}>
                    Сохранить
                </Button>
            </Space>

            <Card loading={loading} style={{ background: '#2d2d3f', border: 'none' }}>
                <Form form={form} layout="vertical">
                    <Text style={{ color: '#aaa', display: 'block', marginBottom: 16 }}>
                        Трудовые правила применяются ко всем сотрудникам тенанта.
                        Изменять может только администратор тенанта.
                    </Text>

                    <Form.Item
                        name="monthCrossingMode"
                        label="Переход смены через полночь (через месяц)"
                        tooltip="Как учитывать часы смены, которая началась в одном месяце и закончилась в следующем"
                    >
                        <Select
                            options={[
                                {
                                    value: 'KEEP_START_DAY',
                                    label: 'Всё в день начала — все часы относятся к дате начала смены',
                                },
                            ]}
                        />
                    </Form.Item>

                    <Form.Item label="Базовый коэффициент">
                        <Space.Compact style={{ width: '100%' }}>
                            <Form.Item
                                name="baseCoefficientName"
                                noStyle
                                rules={[{ required: true, message: 'Укажите название' }]}
                            >
                                <Input placeholder="Название (например «Базовая смена»)" style={{ width: '60%' }} />
                            </Form.Item>
                            <Form.Item
                                name="baseCoefficientValue"
                                noStyle
                                rules={[{ required: true, message: 'Укажите значение' }]}
                            >
                                <InputNumber min={0.01} step={0.1} style={{ width: '40%' }} />
                            </Form.Item>
                        </Space.Compact>
                    </Form.Item>

                    <Form.Item label="Коэффициент за работу в выходной">
                        <Space.Compact style={{ width: '100%' }}>
                            <Form.Item
                                name="weekendCoefficientName"
                                noStyle
                                rules={[{ required: true, message: 'Укажите название' }]}
                            >
                                <Input placeholder="Название (например «Работа в выходной»)" style={{ width: '60%' }} />
                            </Form.Item>
                            <Form.Item
                                name="weekendCoefficientValue"
                                noStyle
                                rules={[{ required: true, message: 'Укажите значение' }]}
                            >
                                <InputNumber min={0.01} step={0.1} style={{ width: '40%' }} />
                            </Form.Item>
                        </Space.Compact>
                    </Form.Item>

                    <Form.Item
                        label="Ночной коэффициент"
                        tooltip="Надбавка за ночные часы. По ТК РФ — от 20 до 40 %."
                    >
                        <Space.Compact style={{ width: '100%' }}>
                            <Form.Item
                                name="nightCoefficientName"
                                noStyle
                                rules={[{ required: true, message: 'Укажите название' }]}
                            >
                                <Input placeholder="Название (например «Ночная надбавка»)" style={{ width: '60%' }} />
                            </Form.Item>
                            <Form.Item
                                name="nightCoefficientValue"
                                noStyle
                                rules={[{ required: true, message: 'Укажите значение' }]}
                            >
                                <InputNumber min={0.01} step={0.01} style={{ width: '40%' }} />
                            </Form.Item>
                        </Space.Compact>
                    </Form.Item>

                    <Text style={{ color: '#888', fontSize: 12 }}>
                        Пример: базовый 1.0 → сотрудник работает за обычную ставку; выходной 2.0 → двойная оплата;
                        ночной 1.2 → +20 % за каждый ночной час.
                    </Text>
                </Form>
            </Card>
        </div>
    );
};

export default TenantSettingsPage;