/**
 * M5 板块管理视图集成测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import SectionManage from './SectionManage.vue'
import { useSectionStore } from '@/stores/modules/section'

describe('SectionManage 视图', () => {
  let pinia

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
  })

  it('应该正确渲染页面标题', () => {
    const wrapper = mount(SectionManage, {
      global: {
        plugins: [pinia]
      }
    })

    expect(wrapper.find('.page-title').text()).toBe('板块管理')
    expect(wrapper.find('.page-description').text()).toBe('管理论坛板块分类和设置')
  })

  it('应该显示添加板块按钮', () => {
    const wrapper = mount(SectionManage, {
      global: {
        plugins: [pinia]
      }
    })

    const addButton = wrapper.find('.btn-primary')
    expect(addButton.exists()).toBe(true)
    expect(addButton.text()).toContain('添加板块')
  })

  it('应该在加载时显示加载状态', () => {
    const sectionStore = useSectionStore()
    sectionStore.loading = true

    const wrapper = mount(SectionManage, {
      global: {
        plugins: [pinia]
      }
    })

    expect(wrapper.find('.loading').exists()).toBe(true)
    expect(wrapper.find('.loading').text()).toBe('加载中...')
  })

  it('应该在没有板块时显示空状态', () => {
    const sectionStore = useSectionStore()
    sectionStore.loading = false
    sectionStore.sections = []

    const wrapper = mount(SectionManage, {
      global: {
        plugins: [pinia]
      }
    })

    expect(wrapper.find('.empty-state').exists()).toBe(true)
    expect(wrapper.find('.empty-state').text()).toContain('暂无板块数据')
  })

  it('应该正确显示板块卡片', () => {
    const sectionStore = useSectionStore()
    sectionStore.loading = false
    sectionStore.sections = [
      {
        id: 1,
        name: '技术讨论',
        slug: 'tech',
        description: '技术相关话题讨论',
        icon: '💻',
        color: '#1976D2',
        posts_count: 100,
        sort_order: 1,
        is_enabled: true
      },
      {
        id: 2,
        name: '生活分享',
        slug: 'life',
        description: '分享生活中的点点滴滴',
        icon: '🎨',
        color: '#4CAF50',
        posts_count: 50,
        sort_order: 2,
        is_enabled: false
      }
    ]

    const wrapper = mount(SectionManage, {
      global: {
        plugins: [pinia]
      }
    })

    const cards = wrapper.findAll('.section-card')
    expect(cards.length).toBe(2)

    // 检查第一个板块
    expect(cards[0].find('.section-name').text()).toBe('技术讨论')
    expect(cards[0].find('.section-slug').text()).toBe('/tech')
    expect(cards[0].find('.section-description').text()).toBe('技术相关话题讨论')
    expect(cards[0].text()).toContain('100')
    expect(cards[0].text()).toContain('启用')

    // 检查第二个板块（禁用状态）
    expect(cards[1].find('.section-name').text()).toBe('生活分享')
    expect(cards[1].classes()).toContain('disabled')
    expect(cards[1].text()).toContain('禁用')
  })

  it('应该在点击添加按钮时打开对话框', async () => {
    const wrapper = mount(SectionManage, {
      global: {
        plugins: [pinia]
      }
    })

    expect(wrapper.find('.dialog').exists()).toBe(false)

    await wrapper.find('.btn-primary').trigger('click')

    expect(wrapper.find('.dialog').exists()).toBe(true)
    expect(wrapper.find('.dialog-header h2').text()).toBe('添加板块')
  })

  it('应该在点击编辑按钮时打开编辑对话框', async () => {
    const sectionStore = useSectionStore()
    sectionStore.loading = false
    sectionStore.sections = [
      {
        id: 1,
        name: '技术讨论',
        slug: 'tech',
        description: '技术相关话题讨论',
        icon: '💻',
        color: '#1976D2',
        posts_count: 100,
        sort_order: 1,
        is_enabled: true
      }
    ]

    const wrapper = mount(SectionManage, {
      global: {
        plugins: [pinia]
      }
    })

    const editButtons = wrapper.findAll('.btn-small')
    await editButtons[0].trigger('click')

    expect(wrapper.find('.dialog').exists()).toBe(true)
    expect(wrapper.find('.dialog-header h2').text()).toBe('编辑板块')
    
    // 检查表单是否填充了数据
    const inputs = wrapper.findAll('.form-input')
    expect(inputs[0].element.value).toBe('tech')
    expect(inputs[1].element.value).toBe('技术讨论')
  })

  it('应该在点击删除按钮时显示确认对话框', async () => {
    const sectionStore = useSectionStore()
    sectionStore.loading = false
    sectionStore.sections = [
      {
        id: 1,
        name: '技术讨论',
        slug: 'tech',
        description: '技术相关话题讨论',
        icon: '💻',
        color: '#1976D2',
        posts_count: 100,
        sort_order: 1,
        is_enabled: true
      }
    ]

    const wrapper = mount(SectionManage, {
      global: {
        plugins: [pinia]
      }
    })

    const deleteButton = wrapper.find('.btn-danger')
    await deleteButton.trigger('click')

    expect(wrapper.findAll('.dialog').length).toBe(1)
    expect(wrapper.text()).toContain('确认删除')
    expect(wrapper.text()).toContain('确定要删除板块"技术讨论"吗？')
  })

  it('应该显示板块的启用/禁用状态', () => {
    const sectionStore = useSectionStore()
    sectionStore.loading = false
    sectionStore.sections = [
      {
        id: 1,
        name: '技术讨论',
        slug: 'tech',
        description: '技术相关话题讨论',
        icon: '💻',
        color: '#1976D2',
        posts_count: 100,
        sort_order: 1,
        is_enabled: true
      },
      {
        id: 2,
        name: '生活分享',
        slug: 'life',
        description: '分享生活中的点点滴滴',
        icon: '🎨',
        color: '#4CAF50',
        posts_count: 50,
        sort_order: 2,
        is_enabled: false
      }
    ]

    const wrapper = mount(SectionManage, {
      global: {
        plugins: [pinia]
      }
    })

    const badges = wrapper.findAll('.status-badge')
    expect(badges[0].classes()).toContain('status-enabled')
    expect(badges[0].text()).toBe('启用')
    
    expect(badges[1].classes()).toContain('status-disabled')
    expect(badges[1].text()).toBe('禁用')
  })

  it('应该显示板块统计信息', () => {
    const sectionStore = useSectionStore()
    sectionStore.loading = false
    sectionStore.sections = [
      {
        id: 1,
        name: '技术讨论',
        slug: 'tech',
        description: '技术相关话题讨论',
        icon: '💻',
        color: '#1976D2',
        posts_count: 100,
        sort_order: 5,
        is_enabled: true
      }
    ]

    const wrapper = mount(SectionManage, {
      global: {
        plugins: [pinia]
      }
    })

    const stats = wrapper.findAll('.stat-value')
    expect(stats[0].text()).toBe('100') // 帖子数
    expect(stats[1].text()).toBe('5') // 排序号
  })

  it('应该在对话框中显示图标选择器', async () => {
    const wrapper = mount(SectionManage, {
      global: {
        plugins: [pinia]
      }
    })

    await wrapper.find('.btn-primary').trigger('click')

    expect(wrapper.find('.icon-selector').exists()).toBe(true)
    
    const iconOptions = wrapper.findAll('.icon-option')
    expect(iconOptions.length).toBeGreaterThan(0)
  })

  it('应该在对话框中显示颜色选择器', async () => {
    const wrapper = mount(SectionManage, {
      global: {
        plugins: [pinia]
      }
    })

    await wrapper.find('.btn-primary').trigger('click')

    expect(wrapper.find('.form-color').exists()).toBe(true)
    expect(wrapper.find('.form-color').attributes('type')).toBe('color')
  })

  it('应该在对话框中显示启用状态开关', async () => {
    const wrapper = mount(SectionManage, {
      global: {
        plugins: [pinia]
      }
    })

    await wrapper.find('.btn-primary').trigger('click')

    expect(wrapper.find('.switch').exists()).toBe(true)
    expect(wrapper.find('.switch input').attributes('type')).toBe('checkbox')
  })

  it('应该在挂载时加载所有板块（包括禁用的）', async () => {
    const sectionStore = useSectionStore()
    const fetchSpy = vi.spyOn(sectionStore, 'fetchSections').mockResolvedValue()

    mount(SectionManage, {
      global: {
        plugins: [pinia]
      }
    })

    await flushPromises()

    expect(fetchSpy).toHaveBeenCalledWith({ enabled_only: false })
  })

  it('应该在提交表单时验证必填项', async () => {
    const wrapper = mount(SectionManage, {
      global: {
        plugins: [pinia]
      }
    })

    await wrapper.find('.btn-primary').trigger('click')

    // 不填写任何内容，直接提交
    const submitButton = wrapper.findAll('.dialog-footer .btn')[1]
    await submitButton.trigger('click')

    await flushPromises()

    // 应该显示错误消息
    expect(wrapper.find('.message-error').exists()).toBe(true)
    expect(wrapper.find('.message-error').text()).toBe('请填写必填项')
  })

  it('应该在成功操作后显示成功消息', async () => {
    const sectionStore = useSectionStore()
    sectionStore.loading = false
    sectionStore.sections = [
      {
        id: 1,
        name: '技术讨论',
        slug: 'tech',
        description: '技术相关话题讨论',
        icon: '💻',
        color: '#1976D2',
        posts_count: 100,
        sort_order: 1,
        is_enabled: true
      }
    ]

    vi.spyOn(sectionStore, 'toggleSectionStatus').mockResolvedValue()
    vi.spyOn(sectionStore, 'fetchSections').mockResolvedValue()

    const wrapper = mount(SectionManage, {
      global: {
        plugins: [pinia]
      }
    })

    // 点击启用/禁用按钮
    const toggleButtons = wrapper.findAll('.btn-small')
    await toggleButtons[1].trigger('click')

    await flushPromises()

    expect(wrapper.find('.message-success').exists()).toBe(true)
  })

  it('应该在编辑时禁用slug输入框', async () => {
    const sectionStore = useSectionStore()
    sectionStore.loading = false
    sectionStore.sections = [
      {
        id: 1,
        name: '技术讨论',
        slug: 'tech',
        description: '技术相关话题讨论',
        icon: '💻',
        color: '#1976D2',
        posts_count: 100,
        sort_order: 1,
        is_enabled: true
      }
    ]

    const wrapper = mount(SectionManage, {
      global: {
        plugins: [pinia]
      }
    })

    const editButtons = wrapper.findAll('.btn-small')
    await editButtons[0].trigger('click')

    const slugInput = wrapper.findAll('.form-input')[0]
    expect(slugInput.attributes('disabled')).toBeDefined()
  })
})
