/**
 * 板块管理 API - Mock 模式
 * 使用 Mock 数据，无需后端服务
 * 
 * 使用方法：
 * 1. 在 main.js 中导入此文件替代 section.js
 * 2. 或者在 .env.development 中设置 VITE_USE_MOCK=true
 */

import { sectionMockApi } from '../mock/sectionMock'

/**
 * 板块API模块（Mock版本）
 */
const sectionApi = {
  /**
   * 获取板块列表（公开接口）
   */
  getList(params = {}) {
    console.log('[Mock API] 获取板块列表', params)
    return sectionMockApi.getList(params)
  },

  /**
   * 获取所有启用的板块（用于下拉选择）
   */
  getAllEnabled() {
    console.log('[Mock API] 获取所有启用板块')
    return sectionMockApi.getAllEnabled()
  },

  /**
   * 获取板块详情（公开接口）
   */
  getDetail(idOrSlug) {
    console.log('[Mock API] 获取板块详情', idOrSlug)
    return sectionMockApi.getDetail(idOrSlug)
  },

  /**
   * 创建板块（管理员接口）
   */
  create(data) {
    console.log('[Mock API] 创建板块', data)
    return sectionMockApi.create(data)
  },

  /**
   * 更新板块（管理员接口）
   */
  update(id, data) {
    console.log('[Mock API] 更新板块', id, data)
    return sectionMockApi.update(id, data)
  },

  /**
   * 删除板块（管理员接口）
   */
  delete(id, permanent = false) {
    console.log('[Mock API] 删除板块', id, permanent)
    return sectionMockApi.delete(id, permanent)
  },

  /**
   * 启用/禁用板块（管理员接口）
   */
  toggleStatus(id, isEnabled) {
    console.log('[Mock API] 切换板块状态', id, isEnabled)
    return sectionMockApi.toggleStatus(id, isEnabled)
  },

  /**
   * 批量调整板块排序（管理员接口）
   */
  reorder(orderData) {
    console.log('[Mock API] 调整排序', orderData)
    return sectionMockApi.reorder(orderData)
  },

  /**
   * 获取板块统计数据（管理员接口）
   */
  getStatistics() {
    console.log('[Mock API] 获取统计数据')
    return sectionMockApi.getStatistics()
  },

  /**
   * 验证板块是否存在（内部接口）
   */
  validate(id) {
    console.log('[Mock API] 验证板块', id)
    return sectionMockApi.validate(id)
  },

  /**
   * 增加板块帖子数（内部接口）
   */
  incrementPosts(id, increment = 1) {
    console.log('[Mock API] 增加帖子数', id, increment)
    return sectionMockApi.incrementPosts(id, increment)
  }
}

export default sectionApi
