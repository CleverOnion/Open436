/**
 * Axios 请求封装
 * 统一配置请求拦截器和响应拦截器
 */
import axios from 'axios'

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
    // TODO: 添加 Token 到请求头
    // 示例：从 localStorage 获取 token
    // const token = localStorage.getItem('token')
    // if (token) {
    //   config.headers['satoken'] = token
    // }
    
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
    
    // TODO: 根据后端约定的响应格式进行处理
    // 示例：
    // if (res.code !== 200) {
    //   // 处理业务错误
    //   return Promise.reject(new Error(res.message || '请求失败'))
    // }
    
    return res
  },
  (error) => {
    // 统一处理错误响应
    console.error('响应错误：', error)
    
    // TODO: 根据错误状态码进行处理
    // 示例：
    // if (error.response) {
    //   switch (error.response.status) {
    //     case 401:
    //       // 未授权，跳转登录页
    //       break
    //     case 403:
    //       // 无权限
    //       break
    //     case 404:
    //       // 资源不存在
    //       break
    //     case 500:
    //       // 服务器错误
    //       break
    //   }
    // }
    
    return Promise.reject(error)
  }
)

export default request

