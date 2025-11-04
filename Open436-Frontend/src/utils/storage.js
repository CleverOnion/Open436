/**
 * localStorage 封装
 * 提供统一的本地存储接口，支持 JSON 序列化
 */

const PREFIX = 'open436_'

export const storage = {
  /**
   * 设置存储
   * @param {string} key - 键名
   * @param {any} value - 值（支持对象，自动序列化）
   */
  set(key, value) {
    try {
      const serializedValue = JSON.stringify(value)
      localStorage.setItem(PREFIX + key, serializedValue)
    } catch (error) {
      console.error('存储失败：', error)
    }
  },

  /**
   * 获取存储
   * @param {string} key - 键名
   * @param {any} defaultValue - 默认值
   * @returns {any} 解析后的值
   */
  get(key, defaultValue = null) {
    try {
      const item = localStorage.getItem(PREFIX + key)
      return item ? JSON.parse(item) : defaultValue
    } catch (error) {
      console.error('读取失败：', error)
      return defaultValue
    }
  },

  /**
   * 移除存储
   * @param {string} key - 键名
   */
  remove(key) {
    localStorage.removeItem(PREFIX + key)
  },

  /**
   * 清空所有存储
   */
  clear() {
    localStorage.clear()
  },

  /**
   * 检查是否存在
   * @param {string} key - 键名
   * @returns {boolean}
   */
  has(key) {
    return localStorage.getItem(PREFIX + key) !== null
  }
}

export default storage

