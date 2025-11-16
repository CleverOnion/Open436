<script setup>
import { ref, onMounted, computed, defineProps, defineEmits } from 'vue'
import { useSectionStore } from '@/stores/modules/section'

// Props
const props = defineProps({
  modelValue: {
    type: [Number, null],
    default: null
  },
  placeholder: {
    type: String,
    default: '请选择板块'
  }
})

// Emits
const emit = defineEmits(['update:modelValue'])

const sectionStore = useSectionStore()
const dropdownVisible = ref(false)

// 计算属性
const sections = computed(() => sectionStore.enabledSections)
const selectedSection = computed(() => {
  if (!props.modelValue) return null
  return sections.value.find((s) => s.id === props.modelValue)
})

// 加载板块列表
onMounted(async () => {
  if (sections.value.length === 0) {
    await loadSections()
  }
})

async function loadSections() {
  try {
    await sectionStore.fetchEnabledSections()
  } catch (error) {
    console.error('加载板块列表失败', error)
  }
}

// 选择板块
function selectSection(section) {
  emit('update:modelValue', section.id)
  dropdownVisible.value = false
}

// 切换下拉菜单
function toggleDropdown() {
  dropdownVisible.value = !dropdownVisible.value
}

// 点击外部关闭
function handleClickOutside(event) {
  if (!event.target.closest('.section-selector')) {
    dropdownVisible.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

// 清理
import { onBeforeUnmount } from 'vue'
onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div class="section-selector">
    <div class="selector-input" @click="toggleDropdown">
      <div v-if="selectedSection" class="selected-section">
        <div class="section-icon" :style="{ background: selectedSection.color + '20', color: selectedSection.color }">
          {{ selectedSection.icon || '📋' }}
        </div>
        <span class="section-name">{{ selectedSection.name }}</span>
      </div>
      <span v-else class="placeholder">{{ placeholder }}</span>
      <svg
        class="arrow-icon"
        :class="{ 'arrow-up': dropdownVisible }"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
      >
        <polyline points="6 9 12 15 18 9"></polyline>
      </svg>
    </div>

    <transition name="dropdown">
      <div v-if="dropdownVisible" class="dropdown-menu">
        <div v-if="sections.length === 0" class="empty-text">暂无可用板块</div>
        <div
          v-for="section in sections"
          :key="section.id"
          class="dropdown-item"
          :class="{ active: section.id === props.modelValue }"
          @click="selectSection(section)"
        >
          <div class="section-icon" :style="{ background: section.color + '20', color: section.color }">
            {{ section.icon || '📋' }}
          </div>
          <div class="section-info">
            <div class="section-name">{{ section.name }}</div>
            <div class="section-description">{{ section.description }}</div>
          </div>
          <svg v-if="section.id === props.modelValue" class="check-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="20 6 9 17 4 12"></polyline>
          </svg>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.section-selector {
  position: relative;
  width: 100%;
}

.selector-input {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.selector-input:hover {
  border-color: #1976d2;
}

.selected-section {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.section-icon {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.section-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.placeholder {
  color: #9ca3af;
  font-size: 14px;
}

.arrow-icon {
  width: 20px;
  height: 20px;
  color: #6b7280;
  transition: transform 0.3s;
  flex-shrink: 0;
}

.arrow-icon.arrow-up {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  max-height: 400px;
  overflow-y: auto;
  z-index: 100;
}

.empty-text {
  padding: 20px;
  text-align: center;
  color: #9ca3af;
  font-size: 14px;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.dropdown-item:hover {
  background: #f9fafb;
}

.dropdown-item.active {
  background: rgba(25, 118, 210, 0.1);
}

.section-info {
  flex: 1;
  min-width: 0;
}

.dropdown-item .section-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
}

.section-description {
  font-size: 12px;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.check-icon {
  width: 20px;
  height: 20px;
  color: #1976d2;
  flex-shrink: 0;
}

/* 过渡动画 */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.3s ease;
}

.dropdown-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 滚动条样式 */
.dropdown-menu::-webkit-scrollbar {
  width: 6px;
}

.dropdown-menu::-webkit-scrollbar-track {
  background: #f9fafb;
  border-radius: 3px;
}

.dropdown-menu::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.dropdown-menu::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}
</style>

