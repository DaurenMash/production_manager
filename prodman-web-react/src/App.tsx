import { RouterProvider } from 'react-router-dom';
import { ConfigProvider, theme } from 'antd';
import { router } from './routes';

function App() {
  return (
    <ConfigProvider
      theme={{
        algorithm: theme.darkAlgorithm,
        token: {
          colorPrimary: '#2E8B57',
          colorBgBase: '#1e1e2f',
          colorBgContainer: '#2d2d3f',
          borderRadius: 8,
        },
      }}
    >
      <RouterProvider router={router} />
    </ConfigProvider>
  );
}

export default App;
