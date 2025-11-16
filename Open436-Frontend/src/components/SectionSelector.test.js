/**
 * M5 板块选择器组件单元测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import SectionSelector from './SectionSelector.vue'
import { useSectionStore } from '@/stores/modules/section'

describe('SectionSelector 组件', () => {
  let pinia

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
  })

  it('应该正确渲染组件', () => {
    const wrapper = mount(SectionSelector, {
      global: {
        plugins: [pinia]
      }
    })

    expect(wrapper.find('.section-selector').exists()).toBe(true)
    expect(wrapper.find('.selector-input').exists()).toBe(true)
  })

  it('应该显示默认占位符', () => {
    const wrapper = mount(SectionSelector, {
      global: {
        plugins: [pinia]
      }
    })

    expect(wrapper.find('.placeholder').text()).toBe('请选择板块')
  })

  it('应该支持自定义占位符', () => {
    const wrapper = mount(SectionSelector, {
      props: {
        placeholder: '选择一个板块'
      },
      global: {
        plugins: [pinia]
      }
    })

    expect(wrapper.find('.placeholder').text()).toBe('选择一个板块')
  })

  it('应该在点击时切换下拉菜单', async () => {
    const wrapper = mount(SectionSelector, {
      global: {
        plugins: [pinia]
      }
    })

    expect(wrapper.find('.dropdown-menu').exists()).toBe(false)

    await wrapper.find('.selector-input').trigger('click')
    expect(wrapper.find('.dropdown-menu').exists()).toBe(true)

    await wrapper.find('.selector-input').trigger('click')
    expect(wrapper.find('.dropdown-menu').exists()).toBe(false)
  })

  it('应该显示已选择的板块', async () => {
    const sectionStore = useSectionStore()
    sectionStore.enabledSections = [
      {
        id: 1,
        name: '技术讨论',
        slug: 'tech',
        icon: '💻',
        color: '#1976D2',
        is_enabled: true
      }
    ]

    const wrapper = mount(SectionSelector, {
      props: {
        modelValue: 1
      },
      global: {
        plugins: [pinia]
      }
    })

    expect(wrapper.find('.selected-section').exists()).toBe(true)
    expect(wrapper.find('.section-name').text()).toBe('技术讨论')
    expect(wrapper.find('.section-icon').text()).toBe('💻')
  })

  it('应该在选择板块时触发 update:modelValue 事件', async () => {
    const sectionStore = useSectionStore()
    sectionStore.enabledSections = [
      {
        id: 1,
        name: '技术讨论',
        slug: 'tech',
        icon: '💻',
        color: '#1976D2',
        is_enabled: true
      },
      {
        id: 2,
        name: '生活分享',
        slug: 'life',
        icon: '🎨',
        color: '#4CAF50',
        is_enabled: true
      }
    ]

    const wrapper = mount(SectionSelector, {
      global: {
        plugins: [pinia]
      }
    })

    await wrapper.find('.selector-input').trigger('click')
    
    const items = wrapper.findAll('.dropdown-item')
    expect(items.length).toBe(2)

    await items[0].trigger('click')

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')[0]).toEqual([1])
  })

  it('应该高亮显示当前选中的板块', async () => {
    const sectionStore = useSectionStore()
    sectionStore.enabledSections = [
      {
        id: 1,
        name: '技术讨论',
        slug: 'tech',
        icon: '💻',
        color: '#1976D2',
        is_enabled: true
      },
      {
        id: 2,
        name: '生活分享',
        slug: 'life',
        icon: '🎨',
        color: '#4CAF50',
        is_enabled: true
      }
    ]

    const wrapper = mount(SectionSelector, {
      props: {
        modelValue: 1
      },
      global: {
        plugins: [pinia]
      }
    })

    await wrapper.find('.selector-input').trigger('click')

    const items = wrapper.findAll('.dropdown-item')
    expect(items[0].classes()).toContain('active')
    expect(items[1].classes()).not.toContain('active')
  })

  it('应该显示空状态提示', async () => {
    const sectionStore = useSectionStore()
    sectionStore.enabledSections = []

    const wrapper = mount(SectionSelector, {
      global: {
        plugins: [pinia]
      }
    })

    await wrapper.find('.selector-input').trigger('click')

    expect(wrapper.find('.empty-text').exists()).toBe(true)
    expect(wrapper.find('.empty-text').text()).toBe('暂无可用板块')
  })

  it('应该在选择后关闭下拉菜单', async () => {
    const sectionStore = useSectionStore()
    sectionStore.enabledSections = [
      {
        id: 1,
        name: '技术讨论',
        slug: 'tech',
        icon: '💻',
        color: '#1976D2',
        is_enabled: true
      }
    ]

    const wrapper = mount(SectionSelector, {
      global: {
        plugins: [pinia]
      }
    })

    await wrapper.find('.selector-input').trigger('click')
    expect(wrapper.find('.dropdown-menu').exists()).toBe(true)

    await wrapper.find('.dropdown-item').trigger('click')
    expect(wrapper.find('.dropdown-menu').exists()).toBe(false)
  })

  it('应该显示选中板块的勾选图标', async () => {
    const sectionStore = useSectionStore()
    sectionStore.enabledSections = [
      {
        id: 1,
        name: '技术讨论',
        slug: 'tech',
        icon: '💻',
        color: '#1976D2',
        is_enabled: true
      }
    ]

    const wrapper = mount(SectionSelector, {
      props: {
        modelValue: 1
      },
      global: {
        plugins: [pinia]
      }
    })

    await wrapper.find('.selector-input').trigger('click')

    expect(wrapper.find('.check-icon').exists()).toBe(true)
  })

  it('应该正确显示板块图标和颜色', async () => {
    const sectionStore = useSectionStore()
    sectionStore.enabledSections = [
      {
        id: 1,
        name: '技术讨论',
        slug: 'tech',
        icon: '💻',
        color: '#1976D2',
        is_enabled: true
      }
    ]

    const wrapper = mount(SectionSelector, {
      props: {
        modelValue: 1
      },
      global: {
        plugins: [pinia]
      }
    })

    const icon = wrapper.find('.section-icon')
    expect(icon.text()).toBe('💻')
    
    const style = icon.attributes('style')
    expect(style).toContain('#1976D2')
  })
})
