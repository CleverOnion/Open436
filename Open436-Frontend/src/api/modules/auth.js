/**
 * 认证服务 API
 * M1 认证服务
 */
import request from '../request'

export const authAPI = {
  /**
   * 用户登录
   * @param {Object} data - 登录数据
   * @param {string} data.username - 用户名（3-20字符）
   * @param {string} data.password - 密码（6-32字符）
   * @param {boolean} data.rememberMe - 是否记住我（可选）
   * @returns {Promise<Object>} 返回包含 token 和用户信息的对象
   */
  login(data) {
    return request.post('/api/auth/login', data)
  },

  /**
   * 用户登出
   * @returns {Promise}
   */
  logout() {
    return request.post('/api/auth/logout')
  },

  /**
   * 验证 Token
   * @param {Object} data - 验证数据
   * @param {string} data.token - 需要验证的 token
   * @returns {Promise<Object>} 返回验证结果
   */
  verify(data) {
    return request.get('/api/auth/verify', { params: data })
  },

  /**
   * 获取当前用户信息
   * @returns {Promise<Object>} 返回当前登录用户信息
   */
  getCurrentUser() {
    return request.get('/api/auth/current')
  },

  /**
   * 修改密码
   * @param {Object} data - 修改密码数据
   * @param {string} data.oldPassword - 原密码
   * @param {string} data.newPassword - 新密码（6-32字符）
   * @param {string} data.confirmPassword - 确认新密码
   * @returns {Promise}
   */
  changePassword(data) {
    return request.put('/api/auth/password', data)
  }
}


