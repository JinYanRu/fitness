import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    host: '0.0.0.0',  // 允许外部访问
    port: 5173,
    proxy: {
      // 后端服务代理
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // OCR 服务代理 (直接调用 RapidOCR - 可选)
      '/ocr-proxy': {
        target: 'http://111.228.49.250:10265',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/ocr-proxy/, '')
      }
    }
  }
})