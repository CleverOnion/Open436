/**
 * 板块管理 API
 * M5 板块管理服务接口封装
 * 
 * 后端服务：Open436-SectionService (Django + DRF)
 * 服务端口：8005
 * API文档：http://localhost:8005/api/docs/
 */
import request from '../request'

/**
 * 板块API模块
 */
const sectionApi = {
  /**
   * 获取板块列表（公开接口）
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码（默认1）
   * @param {number} params.page_size - 每页大小（默认20）
   * @param {string} params.search - 搜索关键词
   * @param {string} params.sort_by - 排序字段（sort_order, name, posts_count, created_at）
   * @param {boolean} params.enabled_only - 仅显示启用的板块（默认true）
   * @returns {Promise} 板块列表数据
   */
  getList(params = {}) {
    // 参数对齐：enabled_only/enabledOnly -> is_enabled；sort_by/sortBy -> ordering
    const {
      enabled_only,
      enabledOnly,
      sort_by,
      sortBy,
      page = 1,
      page_size = 20,
      ...rest
    } = params

    const is_enabled =
      typeof enabled_only !== 'undefined'
        ? enabled_only
        : typeof enabledOnly !== 'undefined'
        ? enabledOnly
        : true

    const ordering = sort_by || sortBy || 'sort_order,id'

    return request({
      url: '/api/sections',
      method: 'get',
      params: {
        page,
        page_size,
        is_enabled,
        ordering,
        ...rest
      }
    })
  },

  /**
   * 获取所有启用的板块（用于下拉选择）
   * @returns {Promise} 板块列表
   */
  getAllEnabled() {
    return request({
      url: '/api/sections',
      method: 'get',
      params: {
        page_size: 100,
        is_enabled: true,
        ordering: 'sort_order,id'
      }
    })
  },

  /**
   * 获取板块详情（公开接口）
   * @param {string|number} idOrSlug - 板块ID或slug标识
   * @returns {Promise} 板块详情数据
   */
  getDetail(idOrSlug) {
    return request({
      url: `/api/sections/${idOrSlug}`,
      method: 'get'
    })
  },

  /**
   * 创建板块（管理员接口）
   * @param {Object} data - 板块数据
   * @param {string} data.slug - 板块标识（3-20个字符，小写字母、数字、下划线）
   * @param {string} data.name - 板块名称（2-50个字符，唯一）
   * @param {string} data.description - 板块描述（最多500字符）
   * @param {string} data.icon - 临时方案：emoji图标字符串（未来将使用 icon_file_id）
   * @param {string} data.icon_file_id - 图标文件ID（可选，UUID格式，需集成M7文件服务）
   * @param {string} data.color - 板块颜色（HEX格式，如 #1976D2）
   * @param {number} data.sort_order - 排序号（1-999）
   * @returns {Promise} 创建的板块数据
   * 
   * @note 当前版本使用 emoji 作为临时图标方案，后端返回 icon_url 字段
   * @todo 集成 M7 文件服务后，支持图片上传并使用 icon_file_id
   */
  create(data) {
    return request({
      url: '/api/sections',
      method: 'post',
      data
    })
  },

  /**
   * 更新板块（管理员接口）
   * @param {number} id - 板块ID
   * @param {Object} data - 更新的板块数据
   * @returns {Promise} 更新后的板块数据
   */
  update(id, data) {
    return request({
      url: `/api/sections/${id}`,
      method: 'put',
      data
    })
  },

  /**
   * 删除板块（管理员接口）
   * @param {number} id - 板块ID
   * @param {boolean} permanent - 是否永久删除（默认false，软删除）
   * @returns {Promise}
   */
  delete(id, permanent = false) {
    return request({
      url: `/api/sections/${id}`,
      method: 'delete',
      // 与 TDD 对齐：使用 force 参数
      params: { force: permanent }
    })
  },

  /**
   * 启用/禁用板块（管理员接口）
   * @param {number} id - 板块ID
   * @param {boolean} isEnabled - 启用状态
   * @returns {Promise} 更新后的板块数据
   */
  toggleStatus(id, isEnabled) {
    return request({
      url: `/api/sections/${id}/status`,
      method: 'put',
      data: { is_enabled: isEnabled }
    })
  },

  /**
   * 批量调整板块排序（管理员接口）
   * @param {Array} orderData - 排序数据数组
   * @param {number} orderData[].id - 板块ID
   * @param {number} orderData[].sort_order - 新的排序号
   * @returns {Promise}
   * 
   * @example
   * reorder([
   *   { id: 1, sort_order: 1 },
   *   { id: 2, sort_order: 2 },
   *   { id: 3, sort_order: 3 }
   * ])
   */
  reorder(orderData) {
    // 支持两种入参：
    // 1) [ { id, sort_order }, ... ] -> 映射为 [id,...]
    // 2) [id1, id2, ...] -> 直接使用
    let order
    if (Array.isArray(orderData)) {
      if (orderData.length > 0 && typeof orderData[0] === 'object') {
        order = orderData.map((x) => x.id)
      } else {
        order = orderData
      }
    } else {
      order = []
    }

    return request({
      url: '/api/sections/reorder',
      method: 'put',
      data: { order }
    })
  },

  /**
   * 获取板块统计数据（管理员接口）
   * @returns {Promise} 统计数据
   */
  getStatistics() {
    return request({
      url: '/api/sections/statistics',
      method: 'get'
    })
  },

  /**
   * 验证板块是否存在（内部接口，供M3调用）
   * @param {number} id - 板块ID
   * @returns {Promise} 验证结果
   */
  validate(id) {
    return request({
      url: `/internal/sections/${id}/validate`,
      method: 'get'
    })
  },

  /**
   * 增加板块帖子数（内部接口，供M3调用）
   * @param {number} id - 板块ID
   * @param {number} increment - 增加数量（可为负数表示减少）
   * @returns {Promise}
   */
  incrementPosts(id, increment = 1) {
    return request({
      url: `/internal/sections/${id}/increment-posts`,
      method: 'post',
      // 与 TDD 对齐：使用 value 字段
      data: { value: increment }
    })
  }
}

export default sectionApi

