import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/user-service': {
        target: 'http://localhost:8888',
        changeOrigin: true,
        secure: false,
      },
      '/employee-service': {
        target: 'http://localhost:8888',
        changeOrigin: true,
        secure: false,
      },
      '/test-service': {
        target: 'http://localhost:8888',
        changeOrigin: true,
        secure: false,
      },
      '/workstation': {
        target: 'http://localhost:8888',
        changeOrigin: true,
        secure: false,
      },
      '/work-calendar': {
        target: 'http://localhost:8888',
        changeOrigin: true,
        secure: false,
      },
    },
  },
})
