/**
 * Axios 请求封装
 * 统一配置请求拦截器和响应拦截器
 */
import axios from 'axios'
import storage from '@/utils/storage'
import router from '@/router'

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
    // 从 storage 获取 token 并添加到请求头
    const token = storage.get('token')
    if (token) {
      // 使用 Bearer 格式
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
    // 统一处理响应数据
    const res = response.data
    
    // 根据后端约定的响应格式进行处理
    // 如果 code 不是 200，视为业务错误
    if (res.code && res.code !== 200) {
      const error = new Error(res.message || '请求失败')
      error.code = res.code
      error.response = { data: res }
      return Promise.reject(error)
    }
    
    return res
  },
  (error) => {
    // 统一处理错误响应
    console.error('响应错误：', error)
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401: {
          // 未授权，清除 token 并跳转登录页
          storage.remove('token')
          storage.remove('userInfo')
          
          // 保存当前路径用于登录后跳转
          const currentPath = router.currentRoute.value.fullPath
          if (currentPath !== '/login') {
            router.push({
              name: 'Login',
              query: { redirect: currentPath }
            })
          }
          break
        }
          
        case 403:
          // 无权限
          console.error('403 无权限访问')
          break
          
        case 404:
          // 资源不存在
          console.error('404 资源不存在')
          break
          
        case 429:
          // 请求过于频繁
          console.error('429 请求过于频繁')
          break
          
        case 500:
          // 服务器错误
          console.error('500 服务器内部错误')
          break
      }
      
      // 将后端返回的错误信息附加到 error 对象
      if (data && data.code) {
        error.code = data.code
        error.message = data.message || error.message
      }
    }
    
    return Promise.reject(error)
  }
)

export default request

