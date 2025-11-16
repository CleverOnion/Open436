/**
 * M5 板块管理 API 单元测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import sectionApi from './section'
import request from '../request'

// Mock request 模块
vi.mock('../request', () => ({
  default: vi.fn()
}))

describe('M5 Section API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getList', () => {
    it('应该使用默认参数获取板块列表', async () => {
      const mockResponse = {
        data: {
          results: [
            { id: 1, name: '技术讨论', slug: 'tech' }
          ]
        }
      }
      request.mockResolvedValue(mockResponse)

      await sectionApi.getList()

      expect(request).toHaveBeenCalledWith({
        url: '/api/sections',
        method: 'get',
        params: {
          page: 1,
          page_size: 20,
          is_enabled: true,
          ordering: 'sort_order,id'
        }
      })
    })

    it('应该支持自定义分页参数', async () => {
      request.mockResolvedValue({ data: {} })

      await sectionApi.getList({
        page: 2,
        page_size: 10
      })

      expect(request).toHaveBeenCalledWith({
        url: '/api/sections',
        method: 'get',
        params: {
          page: 2,
          page_size: 10,
          is_enabled: true,
          ordering: 'sort_order,id'
        }
      })
    })

    it('应该支持 enabled_only 参数', async () => {
      request.mockResolvedValue({ data: {} })

      await sectionApi.getList({ enabled_only: false })

      expect(request).toHaveBeenCalledWith({
        url: '/api/sections',
        method: 'get',
        params: {
          page: 1,
          page_size: 20,
          is_enabled: false,
          ordering: 'sort_order,id'
        }
      })
    })

    it('应该支持 enabledOnly 驼峰命名参数', async () => {
      request.mockResolvedValue({ data: {} })

      await sectionApi.getList({ enabledOnly: false })

      expect(request).toHaveBeenCalledWith({
        url: '/api/sections',
        method: 'get',
        params: {
          page: 1,
          page_size: 20,
          is_enabled: false,
          ordering: 'sort_order,id'
        }
      })
    })

    it('应该支持自定义排序', async () => {
      request.mockResolvedValue({ data: {} })

      await sectionApi.getList({ sort_by: 'name' })

      expect(request).toHaveBeenCalledWith({
        url: '/api/sections',
        method: 'get',
        params: {
          page: 1,
          page_size: 20,
          is_enabled: true,
          ordering: 'name'
        }
      })
    })

    it('应该支持搜索关键词', async () => {
      request.mockResolvedValue({ data: {} })

      await sectionApi.getList({ search: '技术' })

      expect(request).toHaveBeenCalledWith({
        url: '/api/sections',
        method: 'get',
        params: {
          page: 1,
          page_size: 20,
          is_enabled: true,
          ordering: 'sort_order,id',
          search: '技术'
        }
      })
    })
  })

  describe('getAllEnabled', () => {
    it('应该获取所有启用的板块', async () => {
      request.mockResolvedValue({ data: {} })

      await sectionApi.getAllEnabled()

      expect(request).toHaveBeenCalledWith({
        url: '/api/sections',
        method: 'get',
        params: {
          page_size: 100,
          is_enabled: true,
          ordering: 'sort_order,id'
        }
      })
    })
  })

  describe('getDetail', () => {
    it('应该通过ID获取板块详情', async () => {
      request.mockResolvedValue({ data: { id: 1, name: '技术讨论' } })

      await sectionApi.getDetail(1)

      expect(request).toHaveBeenCalledWith({
        url: '/api/sections/1',
        method: 'get'
      })
    })

    it('应该通过slug获取板块详情', async () => {
      request.mockResolvedValue({ data: { id: 1, slug: 'tech' } })

      await sectionApi.getDetail('tech')

      expect(request).toHaveBeenCalledWith({
        url: '/api/sections/tech',
        method: 'get'
      })
    })
  })

  describe('create', () => {
    it('应该创建新板块', async () => {
      const newSection = {
        slug: 'tech',
        name: '技术讨论',
        description: '技术相关话题',
        icon: '💻',
        color: '#1976D2',
        sort_order: 1
      }
      request.mockResolvedValue({ data: { id: 1, ...newSection } })

      await sectionApi.create(newSection)

      expect(request).toHaveBeenCalledWith({
        url: '/api/sections',
        method: 'post',
        data: newSection
      })
    })
  })

  describe('update', () => {
    it('应该更新板块信息', async () => {
      const updateData = {
        name: '技术讨论（更新）',
        description: '新的描述'
      }
      request.mockResolvedValue({ data: { id: 1, ...updateData } })

      await sectionApi.update(1, updateData)

      expect(request).toHaveBeenCalledWith({
        url: '/api/sections/1',
        method: 'put',
        data: updateData
      })
    })
  })

  describe('delete', () => {
    it('应该软删除板块（默认）', async () => {
      request.mockResolvedValue({ data: {} })

      await sectionApi.delete(1)

      expect(request).toHaveBeenCalledWith({
        url: '/api/sections/1',
        method: 'delete',
        params: { force: false }
      })
    })

    it('应该永久删除板块', async () => {
      request.mockResolvedValue({ data: {} })

      await sectionApi.delete(1, true)

      expect(request).toHaveBeenCalledWith({
        url: '/api/sections/1',
        method: 'delete',
        params: { force: true }
      })
    })
  })

  describe('toggleStatus', () => {
    it('应该启用板块', async () => {
      request.mockResolvedValue({ data: { id: 1, is_enabled: true } })

      await sectionApi.toggleStatus(1, true)

      expect(request).toHaveBeenCalledWith({
        url: '/api/sections/1/status',
        method: 'put',
        data: { is_enabled: true }
      })
    })

    it('应该禁用板块', async () => {
      request.mockResolvedValue({ data: { id: 1, is_enabled: false } })

      await sectionApi.toggleStatus(1, false)

      expect(request).toHaveBeenCalledWith({
        url: '/api/sections/1/status',
        method: 'put',
        data: { is_enabled: false }
      })
    })
  })

  describe('reorder', () => {
    it('应该支持对象数组格式的排序', async () => {
      const orderData = [
        { id: 1, sort_order: 1 },
        { id: 2, sort_order: 2 },
        { id: 3, sort_order: 3 }
      ]
      request.mockResolvedValue({ data: {} })

      await sectionApi.reorder(orderData)

      expect(request).toHaveBeenCalledWith({
        url: '/api/sections/reorder',
        method: 'put',
        data: { order: [1, 2, 3] }
      })
    })

    it('应该支持ID数组格式的排序', async () => {
      const orderData = [1, 2, 3]
      request.mockResolvedValue({ data: {} })

      await sectionApi.reorder(orderData)

      expect(request).toHaveBeenCalledWith({
        url: '/api/sections/reorder',
        method: 'put',
        data: { order: [1, 2, 3] }
      })
    })

    it('应该处理空数组', async () => {
      request.mockResolvedValue({ data: {} })

      await sectionApi.reorder([])

      expect(request).toHaveBeenCalledWith({
        url: '/api/sections/reorder',
        method: 'put',
        data: { order: [] }
      })
    })
  })

  describe('getStatistics', () => {
    it('应该获取板块统计数据', async () => {
      const mockStats = {
        total_sections: 5,
        enabled_sections: 4,
        total_posts: 100
      }
      request.mockResolvedValue({ data: mockStats })

      await sectionApi.getStatistics()

      expect(request).toHaveBeenCalledWith({
        url: '/api/sections/statistics',
        method: 'get'
      })
    })
  })

  describe('validate', () => {
    it('应该验证板块是否存在', async () => {
      request.mockResolvedValue({ data: { exists: true } })

      await sectionApi.validate(1)

      expect(request).toHaveBeenCalledWith({
        url: '/internal/sections/1/validate',
        method: 'get'
      })
    })
  })

  describe('incrementPosts', () => {
    it('应该增加板块帖子数（默认+1）', async () => {
      request.mockResolvedValue({ data: {} })

      await sectionApi.incrementPosts(1)

      expect(request).toHaveBeenCalledWith({
        url: '/internal/sections/1/increment-posts',
        method: 'post',
        data: { value: 1 }
      })
    })

    it('应该支持自定义增量', async () => {
      request.mockResolvedValue({ data: {} })

      await sectionApi.incrementPosts(1, 5)

      expect(request).toHaveBeenCalledWith({
        url: '/internal/sections/1/increment-posts',
        method: 'post',
        data: { value: 5 }
      })
    })

    it('应该支持负数（减少帖子数）', async () => {
      request.mockResolvedValue({ data: {} })

      await sectionApi.incrementPosts(1, -1)

      expect(request).toHaveBeenCalledWith({
        url: '/internal/sections/1/increment-posts',
        method: 'post',
        data: { value: -1 }
      })
    })
  })
})
