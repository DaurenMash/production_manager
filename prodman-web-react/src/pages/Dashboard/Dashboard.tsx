import { Typography, Card, Row, Col, Statistic } from 'antd';
const { Title } = Typography;

const Dashboard = () => {
  return (
    <div>
      <Title level={2} style={{ color: 'white' }}>Дашборд</Title>
      <Row gutter={16}>
        <Col span={6}>
          <Card style={{ background: '#2d2d3f', border: 'none' }}>
            <Statistic title="Сотрудники" value={42} valueStyle={{ color: 'white' }} />
          </Card>
        </Col>
        <Col span={6}>
          <Card style={{ background: '#2d2d3f', border: 'none' }}>
            <Statistic title="Активные" value={35} valueStyle={{ color: '#2ecc71' }} />
          </Card>
        </Col>
        <Col span={6}>
          <Card style={{ background: '#2d2d3f', border: 'none' }}>
            <Statistic title="В отпуске" value={5} valueStyle={{ color: '#e67e22' }} />
          </Card>
        </Col>
        <Col span={6}>
          <Card style={{ background: '#2d2d3f', border: 'none' }}>
            <Statistic title="Больничный" value={2} valueStyle={{ color: '#e74c3c' }} />
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Dashboard;
