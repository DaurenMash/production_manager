import { useEffect, useState } from 'react';
import { Typography, Card, Row, Col, Statistic, Alert, Button, Space, message } from 'antd';
import {
    ThunderboltOutlined, DatabaseOutlined, WarningOutlined, CheckCircleOutlined,
    ExportOutlined, ReloadOutlined,
} from '@ant-design/icons';
import apiClient from '../../api/client';

const { Title, Text } = Typography;

interface MetricsSummary {
    servicesUp: number;
    rps: number;
    errorRatePercent: number;
    dbConnectionsActive: number;
}

const GRAFANA_URL = 'http://localhost:3001';

const PlatformMetrics = () => {
    const [summary, setSummary] = useState<MetricsSummary | null>(null);
    const [loading, setLoading] = useState(false);

    const load = async () => {
        setLoading(true);
        try {
            const res = await apiClient
                .get<MetricsSummary>('/user-service/api/v1/platform/metrics/summary')
                .then((r) => r.data);
            setSummary(res);
        } catch (err: any) {
            message.error(err.response?.data?.message || 'Ошибка загрузки метрик');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        load();
        const interval = setInterval(load, 30_000);   // обновление раз в 30 сек
        return () => clearInterval(interval);
    }, []);

    return (
        <div>
            <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }} wrap>
                <Title level={2} style={{ color: 'white', margin: 0 }}>Метрики платформы</Title>
                <Space>
                    <Button icon={<ReloadOutlined />} onClick={load} loading={loading}>
                        Обновить
                    </Button>
                    <Button
                        type="primary"
                        icon={<ExportOutlined />}
                        href={GRAFANA_URL}
                        target="_blank"
                        style={{ background: '#2E8B57', borderColor: '#2E8B57' }}
                    >
                        Открыть Grafana
                    </Button>
                </Space>
            </Space>

            <Alert
                type="info"
                showIcon
                style={{ marginBottom: 16, background: '#1e1e2f', border: '1px solid #3d3d4f' }}
                message={
                    <span>
                        Детальные графики (RPS, latency, JVM, GC, DB) — в{' '}
                        <a href={GRAFANA_URL} target="_blank" rel="noreferrer" style={{ color: '#2E8B57' }}>
                            Grafana
                        </a>
                        . Здесь — сводка с обновлением раз в 30 секунд.
                    </span>
                }
            />

            <Row gutter={16} style={{ marginBottom: 24 }}>
                <Col span={6}>
                    <Card style={{ background: '#2d2d3f', border: 'none' }} loading={loading && !summary}>
                        <Statistic
                            title="Сервисов онлайн"
                            value={summary?.servicesUp ?? '—'}
                            prefix={<CheckCircleOutlined />}
                            valueStyle={{ color: '#2ecc71' }}
                        />
                        <Text style={{ color: '#888', fontSize: 12 }}>up{'{'}job!="api-gateway"{'}'}</Text>
                    </Card>
                </Col>
                <Col span={6}>
                    <Card style={{ background: '#2d2d3f', border: 'none' }} loading={loading && !summary}>
                        <Statistic
                            title="RPS (5m avg)"
                            value={summary?.rps ?? '—'}
                            prefix={<ThunderboltOutlined />}
                            precision={2}
                            valueStyle={{ color: '#3498db' }}
                        />
                        <Text style={{ color: '#888', fontSize: 12 }}>rate(http_server_requests[5m])</Text>
                    </Card>
                </Col>
                <Col span={6}>
                    <Card style={{ background: '#2d2d3f', border: 'none' }} loading={loading && !summary}>
                        <Statistic
                            title="Error rate"
                            value={summary?.errorRatePercent ?? '—'}
                            suffix="%"
                            prefix={<WarningOutlined />}
                            precision={2}
                            valueStyle={{
                                color:
                                    summary && summary.errorRatePercent > 1 ? '#e74c3c' :
                                        summary && summary.errorRatePercent > 0 ? '#e67e22' : '#2ecc71',
                            }}
                        />
                        <Text style={{ color: '#888', fontSize: 12 }}>5xx / total (5m)</Text>
                    </Card>
                </Col>
                <Col span={6}>
                    <Card style={{ background: '#2d2d3f', border: 'none' }} loading={loading && !summary}>
                        <Statistic
                            title="DB connections active"
                            value={summary?.dbConnectionsActive ?? '—'}
                            prefix={<DatabaseOutlined />}
                            valueStyle={{ color: '#9b59b6' }}
                        />
                        <Text style={{ color: '#888', fontSize: 12 }}>hikaricp_connections_active</Text>
                    </Card>
                </Col>
            </Row>

            <Card
                title={<span style={{ color: 'white' }}>По сервисам</span>}
                style={{ background: '#2d2d3f', border: 'none', marginBottom: 16 }}
            >
                <Space direction="vertical" style={{ width: '100%' }}>
                    {[
                        { name: 'user-service', port: 8081, job: 'user-service' },
                        { name: 'work-calendar', port: 8082, job: 'work-calendar' },
                        { name: 'workstation', port: 8083, job: 'workstation' },
                        { name: 'employee-service', port: 8084, job: 'employee-service' },
                        { name: 'test-service', port: 8085, job: 'test-service' },
                        { name: 'prodman-api-gateway', port: 8888, job: 'api-gateway' },
                    ].map((s) => (
                        <div
                            key={s.name}
                            style={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                padding: '8px 12px',
                                background: '#1e1e2f',
                                borderRadius: 6,
                                border: '1px solid #3d3d4f',
                            }}
                        >
                            <Text style={{ color: 'white' }}>{s.name}</Text>
                            <Space>
                                <a
                                    href={`${GRAFANA_URL}/d/spring-boot-3-observability?var-application=${s.job}`}
                                    target="_blank"
                                    rel="noreferrer"
                                    style={{ color: '#2E8B57', fontSize: 12 }}
                                >
                                    Grafana →
                                </a>
                            </Space>
                        </div>
                    ))}
                </Space>
            </Card>
        </div>
    );
};

export default PlatformMetrics;