/**
 * Axios 请求封装
 * 统一配置请求拦截器和响应拦截器
 */
import axios from 'axios'
import storage from '@/utils/storage'

// 创建 axios 实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8000',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 注入 Authorization 头（Sa-Token）
    const token = storage.get('token')
    if (token) {
      config.headers = config.headers || {}
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 统一处理响应数据：后端约定 { code, message, data, timestamp }
    const res = response.data
    if (res && typeof res === 'object' && 'code' in res) {
      // 非 2xx 业务码也可能返回 200 HTTP，这里按 code 判断
      if (res.code >= 200 && res.code < 300) {
        return res.data
      }
      const err = new Error(res.message || '请求失败')
      err.code = res.code
      err.response = response
      return Promise.reject(err)
    }
    // 兼容未包装的响应
    return res
  },
  (error) => {
    // 统一处理错误响应
    const status = error?.response?.status
    if (status === 401) {
      // 未认证：可在此处跳转登录或清理本地状态
      // 留空以避免副作用；交由调用方决定
    } else if (status === 403) {
      // 权限不足：可提示或跳转 403 页面
    }
    console.error('响应错误：', error)
    return Promise.reject(error)
  }
)

export default request
