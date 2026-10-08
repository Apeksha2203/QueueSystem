// VIVA GUIDE: Student development/build configuration: React plugin and /api proxy. The local proxy differs from production Netlify rewrites.
import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const proxy = { '/api': { target: env.QUEUE_BACKEND_URL || 'http://127.0.0.1:8081', changeOrigin: true } }
  return {
  plugins: [react()],
  server: {
    proxy
  },
  preview: { proxy }
  }
})
