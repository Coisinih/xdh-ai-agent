import axios from 'axios'

/**
 * 根据环境区分 API 基础地址：
 * 生产环境使用相对路径，适合同域部署；
 * 开发环境直连本地后端服务。
 */
const API_BASE_URL = import.meta.env.PROD
  ? '/api'
  : 'http://localhost:8123/api'

const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60_000,
})

export default http
