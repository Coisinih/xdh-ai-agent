import axios from 'axios'

/**
 * 开发环境通过 Vite 代理访问 /api；生产可改为完整后端地址。
 */
const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 60_000,
})

export default http
