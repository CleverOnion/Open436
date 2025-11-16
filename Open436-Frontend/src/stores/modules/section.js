/**
 * 板块管理 Store
 * 使用 Pinia 管理板块相关状态
 */
import { defineStore } from 'pinia'

// 根据环境变量决定使用真实API还是Mock API
const useMock = import.meta.env.VITE_USE_MOCK === 'true'
const sectionApi = useMock 
  ? await import('@/api/modules/section.mock').then(m => m.default)
  : await import('@/api/modules/section').then(m => m.default)

if (useMock) {
  console.log('🎭 [M5] 使用 Mock 数据模式')
} else {
  console.log('🌐 [M5] 使用真实 API 模式')
}

export const useSectionStore = defineStore('section', {
  state: () => ({
    // 板块列表
    sections: [],
    // 所有启用的板块（用于选择器）
    enabledSections: [],
    // 当前板块详情
    currentSection: null,
    // 加载状态
    loading: false,
    // 分页信息
    pagination: {
      page: 1,
      pageSize: 20,
      total: 0,
      totalPages: 0
    },
    // 筛选和排序
    filters: {
      search: '',
      sortBy: 'sort_order',
      enabledOnly: true
    },
    // 统计数据
    statistics: null
  }),

  getters: {
    /**
     * 获取排序后的板块列表
     */
    sortedSections: (state) => {
      return [...state.sections].sort((a, b) => {
        // 按 sort_order 升序排序
        return a.sort_order - b.sort_order
      })
    },

    /**
     * 根据ID获取板块
     * @param {number} id
     */
    getSectionById: (state) => (id) => {
      return state.sections.find((section) => section.id === id)
    },

    /**
     * 根据slug获取板块
     * @param {string} slug
     */
    getSectionBySlug: (state) => (slug) => {
      return state.sections.find((section) => section.slug === slug)
    },

    /**
     * 检查是否有更多数据
     */
    hasMore: (state) => {
      return state.pagination.page < state.pagination.totalPages
    }
  },

  actions: {
    /**
     * 获取板块列表
     * @param {Object} params - 查询参数
     */
    async fetchSections(params = {}) {
      this.loading = true
      try {
        const response = await sectionApi.getList({
          ...this.filters,
          page: this.pagination.page,
          page_size: this.pagination.pageSize,
          ...params
        })

        // 假设后端返回格式为 { count, results, next, previous }
        this.sections = response.results || response.data || []
        this.pagination.total = response.count || 0
        this.pagination.totalPages = Math.ceil(this.pagination.total / this.pagination.pageSize)

        return response
      } catch (error) {
        console.error('获取板块列表失败：', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取所有启用的板块（用于下拉选择）
     */
    async fetchEnabledSections() {
      try {
        const response = await sectionApi.getAllEnabled()
        this.enabledSections = response.results || response.data || []
        return response
      } catch (error) {
        console.error('获取启用板块失败：', error)
        throw error
      }
    },

    /**
     * 获取板块详情
     * @param {string|number} idOrSlug
     */
    async fetchSectionDetail(idOrSlug) {
      this.loading = true
      try {
        const response = await sectionApi.getDetail(idOrSlug)
        this.currentSection = response.data || response
        return response
      } catch (error) {
        console.error('获取板块详情失败：', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 创建板块
     * @param {Object} data
     */
    async createSection(data) {
      this.loading = true
      try {
        const response = await sectionApi.create(data)
        // 添加到列表中
        this.sections.unshift(response.data || response)
        return response
      } catch (error) {
        console.error('创建板块失败：', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 更新板块
     * @param {number} id
     * @param {Object} data
     */
    async updateSection(id, data) {
      this.loading = true
      try {
        const response = await sectionApi.update(id, data)
        // 更新列表中的数据
        const index = this.sections.findIndex((s) => s.id === id)
        if (index !== -1) {
          this.sections[index] = response.data || response
        }
        // 更新当前板块
        if (this.currentSection && this.currentSection.id === id) {
          this.currentSection = response.data || response
        }
        return response
      } catch (error) {
        console.error('更新板块失败：', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 删除板块
     * @param {number} id
     * @param {boolean} permanent
     */
    async deleteSection(id, permanent = false) {
      this.loading = true
      try {
        await sectionApi.delete(id, permanent)
        // 从列表中移除
        this.sections = this.sections.filter((s) => s.id !== id)
        return true
      } catch (error) {
        console.error('删除板块失败：', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 启用/禁用板块
     * @param {number} id
     * @param {boolean} isEnabled
     */
    async toggleSectionStatus(id, isEnabled) {
      try {
        const response = await sectionApi.toggleStatus(id, isEnabled)
        // 更新列表中的数据
        const index = this.sections.findIndex((s) => s.id === id)
        if (index !== -1) {
          this.sections[index].is_enabled = isEnabled
        }
        return response
      } catch (error) {
        console.error('切换板块状态失败：', error)
        throw error
      }
    },

    /**
     * 批量调整排序
     * @param {Array} orderData
     */
    async reorderSections(orderData) {
      this.loading = true
      try {
        await sectionApi.reorder(orderData)
        // 重新获取列表
        await this.fetchSections()
        return true
      } catch (error) {
        console.error('调整排序失败：', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取统计数据
     */
    async fetchStatistics() {
      try {
        const response = await sectionApi.getStatistics()
        this.statistics = response.data || response
        return response
      } catch (error) {
        console.error('获取统计数据失败：', error)
        throw error
      }
    },

    /**
     * 设置筛选条件
     * @param {Object} filters
     */
    setFilters(filters) {
      this.filters = { ...this.filters, ...filters }
    },

    /**
     * 设置当前页码
     * @param {number} page
     */
    setPage(page) {
      this.pagination.page = page
    },

    /**
     * 重置筛选和分页
     */
    resetFilters() {
      this.filters = {
        search: '',
        sortBy: 'sort_order',
        enabledOnly: true
      }
      this.pagination.page = 1
    },

    /**
     * 清空当前板块详情
     */
    clearCurrentSection() {
      this.currentSection = null
    }
  }
})

