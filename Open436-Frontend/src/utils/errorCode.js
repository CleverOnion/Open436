/**
 * 错误码映射
 * 根据 M1 认证服务接口文档定义
 */

// 认证模块错误码映射
export const AUTH_ERROR_CODES = {
  // 400 - 参数错误
  40001001: '用户名不能为空',
  40001002: '密码不能为空',
  40001003: '新密码长度不符合要求',
  40001004: '两次输入的密码不一致',
  40001005: '新密码不能与原密码相同',
  40001006: '用户名长度不符合要求（3-20个字符）',
  40001007: '状态值无效',

  // 401 - 未授权
  40101001: '用户名或密码错误',
  40101002: 'Token 已过期，请重新登录',
  40101003: 'Token 无效，请重新登录',
  40101004: '原密码错误',

  // 403 - 禁止访问
  40301001: '账号已被禁用，请联系管理员',
  40301002: '需要管理员权限',

  // 404 - 资源不存在
  40401001: '用户不存在',
  40401002: '角色不存在',

  // 409 - 冲突
  40901001: '用户名已存在',

  // 429 - 请求过于频繁
  42900001: '登录尝试次数过多，请稍后再试',

  // 500 - 服务器错误
  50000000: '服务器内部错误，请稍后再试'
}

/**
 * 获取错误信息
 * @param {number|string} code - 错误码
 * @param {string} defaultMessage - 默认错误信息
 * @returns {string} 错误信息
 */
export function getErrorMessage(code, defaultMessage = '请求失败，请稍后再试') {
  // 转换为数字
  const errorCode = typeof code === 'string' ? parseInt(code, 10) : code
  
  // 返回对应的错误信息，如果没有则返回默认信息
  return AUTH_ERROR_CODES[errorCode] || defaultMessage
}

/**
 * 根据 HTTP 状态码获取友好提示
 * @param {number} status - HTTP 状态码
 * @returns {string} 错误提示
 */
export function getHttpStatusMessage(status) {
  const statusMessages = {
    400: '请求参数错误',
    401: '未授权，请重新登录',
    403: '拒绝访问，权限不足',
    404: '请求的资源不存在',
    408: '请求超时',
    429: '请求过于频繁，请稍后再试',
    500: '服务器内部错误',
    502: '网关错误',
    503: '服务暂时不可用',
    504: '网关超时'
  }
  
  return statusMessages[status] || `请求失败 (${status})`
}

/**
 * 处理 API 错误
 * @param {Error} error - 错误对象
 * @returns {string} 用户友好的错误信息
 */
export function handleApiError(error) {
  // 如果有业务错误码，优先使用业务错误码
  if (error.code) {
    return getErrorMessage(error.code, error.message)
  }
  
  // 如果有响应对象
  if (error.response) {
    const { status, data } = error.response
    
    // 如果响应数据中有错误码
    if (data && data.code) {
      return getErrorMessage(data.code, data.message)
    }
    
    // 如果响应数据中有错误信息
    if (data && data.message) {
      return data.message
    }
    
    // 使用 HTTP 状态码提示
    return getHttpStatusMessage(status)
  }
  
  // 网络错误
  if (error.message === 'Network Error') {
    return '网络连接失败，请检查网络设置'
  }
  
  // 请求超时
  if (error.code === 'ECONNABORTED') {
    return '请求超时，请稍后再试'
  }
  
  // 返回原始错误信息或默认信息
  return error.message || '请求失败，请稍后再试'
}

export default {
  AUTH_ERROR_CODES,
  getErrorMessage,
  getHttpStatusMessage,
  handleApiError
}

