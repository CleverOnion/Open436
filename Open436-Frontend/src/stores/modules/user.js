/**
 * 用户状态管理模块
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import storage from '@/utils/storage'
import { authAPI } from '@/api/modules/auth'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(storage.get('token', ''))
  const userInfo = ref(storage.get('userInfo', null))
  const expiresIn = ref(storage.get('expiresIn', 0))

  // 计算属性
  const isLoggedIn = computed(() => {
    return !!token.value
  })

  // 获取用户角色
  const userRole = computed(() => {
    return userInfo.value?.role || 'user'
  })

  // 获取用户状态
  const userStatus = computed(() => {
    return userInfo.value?.status || 'active'
  })

  // 是否为管理员
  const isAdmin = computed(() => {
    return userRole.value === 'admin'
  })

  /**
   * 登录方法
   * @param {string} loginToken - 登录令牌
   * @param {Object} info - 用户信息
   * @param {number} info.id - 用户ID
   * @param {string} info.username - 用户名
   * @param {string} info.role - 角色
   * @param {string} info.status - 状态
   * @param {number} expires - token 过期时间（秒）
   */
  const login = (loginToken, info, expires = 2592000) => {
    token.value = loginToken
    userInfo.value = info
    expiresIn.value = expires
    
    // 持久化到 localStorage
    storage.set('token', loginToken)
    storage.set('userInfo', info)
    storage.set('expiresIn', expires)
  }

  /**
   * 登出方法
   * @param {boolean} callApi - 是否调用后端登出接口
   */
  const logout = async (callApi = true) => {
    try {
      // 如果有 token，调用后端登出接口
      if (callApi && token.value) {
        await authAPI.logout()
      }
    } catch (error) {
      console.error('登出接口调用失败：', error)
    } finally {
      // 无论接口是否成功，都清除本地数据
      token.value = ''
      userInfo.value = null
      expiresIn.value = 0
      
      // 清除 localStorage
      storage.remove('token')
      storage.remove('userInfo')
      storage.remove('expiresIn')
    }
  }

  /**
   * 更新用户信息
   * @param {Object} info - 用户信息
   */
  const setUserInfo = (info) => {
    userInfo.value = { ...userInfo.value, ...info }
    storage.set('userInfo', userInfo.value)
  }

  /**
   * 更新 token
   * @param {string} newToken - 新的 token
   */
  const setToken = (newToken) => {
    token.value = newToken
    storage.set('token', newToken)
  }

  /**
   * 获取当前用户信息（从服务器）
   */
  const fetchUserInfo = async () => {
    try {
      const response = await authAPI.getCurrentUser()
      if (response.code === 200 && response.data) {
        setUserInfo(response.data.user || response.data)
      }
      return response
    } catch (error) {
      console.error('获取用户信息失败：', error)
      throw error
    }
  }

  return {
    // 状态
    token,
    userInfo,
    expiresIn,
    // 计算属性
    isLoggedIn,
    userRole,
    userStatus,
    isAdmin,
    // 方法
    login,
    logout,
    setUserInfo,
    setToken,
    fetchUserInfo
  }
})

