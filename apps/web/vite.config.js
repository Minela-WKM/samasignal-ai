import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [vue(), tailwindcss()],
  server: {
    proxy: {
      '/api': 'http://127.0.0.1:8081',
      '/actuator': 'http://127.0.0.1:8081',
      '/swagger-ui': 'http://127.0.0.1:8081',
      '/v3/api-docs': 'http://127.0.0.1:8081',
    },
  },
})
