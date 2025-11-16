/**
 * M5 板块管理模块 Mock 数据
 * 用于前端独立开发和测试，无需后端服务
 */

// 模拟板块数据
let mockSections = [
  {
    id: 1,
    slug: 'tech',
    name: '技术交流',
    description: '分享编程技术和开发经验，讨论最新技术趋势',
    icon: '💻',
    icon_url: null,
    color: '#1976D2',
    sort_order: 1,
    is_enabled: true,
    posts_count: 156,
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z'
  },
  {
    id: 2,
    slug: 'design',
    name: '设计分享',
    description: 'UI/UX 设计作品展示、设计心得分享',
    icon: '🎨',
    icon_url: null,
    color: '#9C27B0',
    sort_order: 2,
    is_enabled: true,
    posts_count: 89,
    created_at: '2024-01-02T00:00:00Z',
    updated_at: '2024-01-02T00:00:00Z'
  },
  {
    id: 3,
    slug: 'discuss',
    name: '综合讨论',
    description: '各类话题的自由讨论',
    icon: '💬',
    icon_url: null,
    color: '#4CAF50',
    sort_order: 3,
    is_enabled: true,
    posts_count: 234,
    created_at: '2024-01-03T00:00:00Z',
    updated_at: '2024-01-03T00:00:00Z'
  },
  {
    id: 4,
    slug: 'question',
    name: '问答求助',
    description: '技术问题求助和解答',
    icon: '❓',
    icon_url: null,
    color: '#FF9800',
    sort_order: 4,
    is_enabled: true,
    posts_count: 178,
    created_at: '2024-01-04T00:00:00Z',
    updated_at: '2024-01-04T00:00:00Z'
  },
  {
    id: 5,
    slug: 'share',
    name: '资源分享',
    description: '工具、教程等资源推荐',
    icon: '📦',
    icon_url: null,
    color: '#00BCD4',
    sort_order: 5,
    is_enabled: true,
    posts_count: 92,
    created_at: '2024-01-05T00:00:00Z',
    updated_at: '2024-01-05T00:00:00Z'
  },
  {
    id: 6,
    slug: 'announce',
    name: '公告通知',
    description: '官方公告和重要通知',
    icon: '📢',
    icon_url: null,
    color: '#F44336',
    sort_order: 6,
    is_enabled: true,
    posts_count: 23,
    created_at: '2024-01-06T00:00:00Z',
    updated_at: '2024-01-06T00:00:00Z'
  },
  {
    id: 7,
    slug: 'test-disabled',
    name: '测试禁用板块',
    description: '这是一个被禁用的测试板块',
    icon: '🚫',
    icon_url: null,
    color: '#9E9E9E',
    sort_order: 99,
    is_enabled: false,
    posts_count: 0,
    created_at: '2024-01-07T00:00:00Z',
    updated_at: '2024-01-07T00:00:00Z'
  }
]

// 模拟延迟
const delay = (ms = 300) => new Promise(resolve => setTimeout(resolve, ms))

// 生成新ID
let nextId = 8

/**
 * Mock API 实现
 */
export const sectionMockApi = {
  /**
   * 获取板块列表
   */
  async getList(params = {}) {
    await delay()
    
    const {
      page = 1,
      page_size = 20,
      is_enabled,
      ordering = 'sort_order,id',
      search = ''
    } = params

    // 筛选
    let filtered = [...mockSections]
    
    // 按启用状态筛选
    if (typeof is_enabled !== 'undefined') {
      filtered = filtered.filter(s => s.is_enabled === is_enabled)
    }
    
    // 搜索
    if (search) {
      filtered = filtered.filter(s => 
        s.name.includes(search) || 
        s.description.includes(search) ||
        s.slug.includes(search)
      )
    }
    
    // 排序
    if (ordering.includes('sort_order')) {
      filtered.sort((a, b) => a.sort_order - b.sort_order)
    } else if (ordering.includes('name')) {
      filtered.sort((a, b) => a.name.localeCompare(b.name))
    } else if (ordering.includes('posts_count')) {
      filtered.sort((a, b) => b.posts_count - a.posts_count)
    }
    
    // 分页
    const start = (page - 1) * page_size
    const end = start + page_size
    const results = filtered.slice(start, end)
    
    return {
      count: filtered.length,
      next: end < filtered.length ? `?page=${page + 1}` : null,
      previous: page > 1 ? `?page=${page - 1}` : null,
      results
    }
  },

  /**
   * 获取所有启用的板块
   */
  async getAllEnabled() {
    await delay()
    
    const results = mockSections.filter(s => s.is_enabled)
      .sort((a, b) => a.sort_order - b.sort_order)
    
    return {
      count: results.length,
      results
    }
  },

  /**
   * 获取板块详情
   */
  async getDetail(idOrSlug) {
    await delay()
    
    const section = mockSections.find(s => 
      s.id === Number(idOrSlug) || s.slug === idOrSlug
    )
    
    if (!section) {
      throw new Error('板块不存在')
    }
    
    return section
  },

  /**
   * 创建板块
   */
  async create(data) {
    await delay()
    
    // 验证
    if (mockSections.some(s => s.slug === data.slug)) {
      throw new Error('板块标识已存在')
    }
    if (mockSections.some(s => s.name === data.name)) {
      throw new Error('板块名称已存在')
    }
    
    const newSection = {
      id: nextId++,
      slug: data.slug,
      name: data.name,
      description: data.description || '',
      icon: data.icon || '📋',
      icon_url: data.icon_file_id ? `http://mock.com/icon/${data.icon_file_id}` : null,
      color: data.color,
      sort_order: data.sort_order || 100,
      is_enabled: typeof data.is_enabled !== 'undefined' ? data.is_enabled : true,
      posts_count: 0,
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString()
    }
    
    mockSections.push(newSection)
    return newSection
  },

  /**
   * 更新板块
   */
  async update(id, data) {
    await delay()
    
    const index = mockSections.findIndex(s => s.id === id)
    if (index === -1) {
      throw new Error('板块不存在')
    }
    
    // 验证名称唯一性
    if (data.name && mockSections.some(s => s.id !== id && s.name === data.name)) {
      throw new Error('板块名称已存在')
    }
    
    const updated = {
      ...mockSections[index],
      ...data,
      id, // 保持ID不变
      slug: mockSections[index].slug, // slug 不可修改
      updated_at: new Date().toISOString()
    }
    
    mockSections[index] = updated
    return updated
  },

  /**
   * 删除板块
   */
  async delete(id, permanent = false) {
    await delay()
    
    const index = mockSections.findIndex(s => s.id === id)
    if (index === -1) {
      throw new Error('板块不存在')
    }
    
    if (permanent) {
      // 硬删除
      mockSections.splice(index, 1)
    } else {
      // 软删除（禁用）
      mockSections[index].is_enabled = false
    }
    
    return { message: '删除成功' }
  },

  /**
   * 切换启用状态
   */
  async toggleStatus(id, isEnabled) {
    await delay()
    
    const index = mockSections.findIndex(s => s.id === id)
    if (index === -1) {
      throw new Error('板块不存在')
    }
    
    mockSections[index].is_enabled = isEnabled
    mockSections[index].updated_at = new Date().toISOString()
    
    return mockSections[index]
  },

  /**
   * 批量调整排序
   */
  async reorder(orderData) {
    await delay()
    
    // 支持两种格式
    const order = Array.isArray(orderData) && orderData.length > 0 && typeof orderData[0] === 'object'
      ? orderData.map(x => x.id)
      : orderData
    
    // 更新排序
    order.forEach((id, index) => {
      const section = mockSections.find(s => s.id === id)
      if (section) {
        section.sort_order = index + 1
        section.updated_at = new Date().toISOString()
      }
    })
    
    return { message: '排序更新成功' }
  },

  /**
   * 获取统计数据
   */
  async getStatistics() {
    await delay()
    
    const enabled = mockSections.filter(s => s.is_enabled)
    const totalPosts = mockSections.reduce((sum, s) => sum + s.posts_count, 0)
    
    return {
      total_sections: mockSections.length,
      enabled_sections: enabled.length,
      disabled_sections: mockSections.length - enabled.length,
      total_posts: totalPosts,
      average_posts: totalPosts / mockSections.length
    }
  },

  /**
   * 验证板块（内部接口）
   */
  async validate(id) {
    await delay()
    
    const section = mockSections.find(s => s.id === id)
    return {
      exists: !!section,
      is_enabled: section?.is_enabled || false
    }
  },

  /**
   * 增加帖子数（内部接口）
   */
  async incrementPosts(id, increment = 1) {
    await delay()
    
    const section = mockSections.find(s => s.id === id)
    if (section) {
      section.posts_count += increment
      section.updated_at = new Date().toISOString()
    }
    
    return { message: '更新成功' }
  }
}

/**
 * 重置 Mock 数据（用于测试）
 */
export function resetMockData() {
  mockSections = [
    {
      id: 1,
      slug: 'tech',
      name: '技术交流',
      description: '分享编程技术和开发经验',
      icon: '💻',
      icon_url: null,
      color: '#1976D2',
      sort_order: 1,
      is_enabled: true,
      posts_count: 156,
      created_at: '2024-01-01T00:00:00Z',
      updated_at: '2024-01-01T00:00:00Z'
    }
  ]
  nextId = 2
}

export default sectionMockApi
