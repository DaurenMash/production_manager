import { useNavigate, Link } from 'react-router-dom';
import { Button, Card, Row, Col, Typography, Space, Tag } from 'antd';
import {
    CalendarOutlined,
    BarChartOutlined,
    TeamOutlined,
    AppstoreOutlined,
    SafetyCertificateOutlined,
    ThunderboltOutlined,
    CheckCircleOutlined,
    ArrowRightOutlined,
} from '@ant-design/icons';

const { Title, Text, Paragraph } = Typography;

const FEATURES = [
    {
        icon: <TeamOutlined style={{ fontSize: 28, color: '#2E8B57' }} />,
        title: 'Сотрудники и отделы',
        description: 'Справочники сотрудников, отделов, должностей и квалификаций с полной изоляцией данных по тенантам.',
    },
    {
        icon: <AppstoreOutlined style={{ fontSize: 28, color: '#2E8B57' }} />,
        title: 'Рабочие станции',
        description: 'Гибкая настройка рабочих станций: от горячего цеха до водителя и курьера. Привязка квалификаций.',
    },
    {
        icon: <CalendarOutlined style={{ fontSize: 28, color: '#2E8B57' }} />,
        title: 'Календарь',
        description: 'Расстановка сотрудников по станциям с проверкой квалификаций, учёт ночных часов и переработок.',
    },
    {
        icon: <BarChartOutlined style={{ fontSize: 28, color: '#2E8B57' }} />,
        title: 'Отчёты',
        description: 'Часы по сотрудникам, отделам, станциям. Экспорт в CSV. Коэффициенты оплаты настраиваются.',
    },
    {
        icon: <SafetyCertificateOutlined style={{ fontSize: 28, color: '#2E8B57' }} />,
        title: 'Тесты квалификации',
        description: 'Проверка квалификаций сотрудников через встроенные тесты (в разработке).',
    },
    {
        icon: <ThunderboltOutlined style={{ fontSize: 28, color: '#2E8B57' }} />,
        title: 'Импорт и интеграции',
        description: 'Готовые API для интеграции с вашими системами. REST + JWT + мультитенантность.',
    },
];

const DemoPage = () => {
    const navigate = useNavigate();

    return (
        <div
            style={{
                minHeight: '100vh',
                background: 'linear-gradient(to bottom, #1a1a2e, #16213e)',
                padding: '48px 24px',
            }}
        >
            <div style={{ maxWidth: 1200, margin: '0 auto' }}>
                {/* Hero */}
                <div style={{ textAlign: 'center', marginBottom: 64 }}>
                    <Tag color="green" style={{ marginBottom: 16, fontSize: 13, padding: '4px 12px' }}>
                        <CheckCircleOutlined /> 30 дней бесплатно
                    </Tag>
                    <Title level={1} style={{ color: '#2E8B57', margin: 0, fontSize: 56 }}>
                        PRODMAN
                    </Title>
                    <Title level={3} style={{ color: 'white', marginTop: 8, fontWeight: 400 }}>
                        Управление производством без Excel
                    </Title>
                    <Paragraph style={{ color: '#aaa', fontSize: 18, maxWidth: 720, margin: '16px auto 32px' }}>
                        Расставляйте сотрудников по рабочим станциям, считайте часы, следите за квалификациями.
                        Всё в одном месте, с полной изоляцией данных по организациям.
                    </Paragraph>
                    <Space size="large">
                        <Button
                            type="primary"
                            size="large"
                            icon={<ArrowRightOutlined />}
                            onClick={() => navigate('/register')}
                            style={{ background: '#2E8B57', borderColor: '#2E8B57', height: 48, padding: '0 32px' }}
                        >
                            Начать бесплатно
                        </Button>
                        <Button
                            size="large"
                            onClick={() => navigate('/login')}
                            style={{ height: 48, padding: '0 32px' }}
                        >
                            Войти
                        </Button>
                    </Space>
                </div>

                {/* Features */}
                <Title level={2} style={{ color: 'white', textAlign: 'center', marginBottom: 32 }}>
                    Что внутри
                </Title>
                <Row gutter={[24, 24]}>
                    {FEATURES.map((f) => (
                        <Col xs={24} sm={12} lg={8} key={f.title}>
                            <Card
                                style={{
                                    background: '#2d2d3f',
                                    border: '1px solid #3d3d4f',
                                    height: '100%',
                                    transition: 'border-color 0.2s',
                                }}
                                hoverable
                                styles={{ body: { padding: 24 } }}
                            >
                                <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                                    {f.icon}
                                    <Text style={{ color: 'white', fontSize: 18, fontWeight: 600 }}>
                                        {f.title}
                                    </Text>
                                    <Text style={{ color: '#aaa', fontSize: 14 }}>
                                        {f.description}
                                    </Text>
                                </Space>
                            </Card>
                        </Col>
                    ))}
                </Row>

                {/* CTA */}
                <div
                    style={{
                        textAlign: 'center',
                        marginTop: 64,
                        padding: 48,
                        background: '#2d2d3f',
                        borderRadius: 16,
                        border: '1px solid #3d3d4f',
                    }}
                >
                    <Title level={2} style={{ color: 'white', marginBottom: 8 }}>
                        Готовы попробовать?
                    </Title>
                    <Text style={{ color: '#aaa', fontSize: 16, display: 'block', marginBottom: 24 }}>
                        30 дней бесплатно. Без карты. Регистрация за 30 секунд.
                    </Text>
                    <Button
                        type="primary"
                        size="large"
                        icon={<ArrowRightOutlined />}
                        onClick={() => navigate('/register')}
                        style={{ background: '#2E8B57', borderColor: '#2E8B57', height: 48, padding: '0 32px' }}
                    >
                        Создать организацию
                    </Button>
                </div>

                {/* Footer */}
                <div style={{ textAlign: 'center', marginTop: 48, color: '#666', fontSize: 13 }}>
                    <Space split={<span style={{ color: '#333' }}>·</span>}>
                        <span>© {new Date().getFullYear()} ProdMan</span>
                        <Link to="/login" style={{ color: '#666' }}>Вход</Link>
                        <Link to="/register" style={{ color: '#666' }}>Регистрация</Link>
                    </Space>
                </div>
            </div>
        </div>
    );
};

export default DemoPage;