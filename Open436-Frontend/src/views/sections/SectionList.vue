<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useSectionStore } from '@/stores/modules/section'

const router = useRouter()
const sectionStore = useSectionStore()

// 计算属性
const sections = computed(() => sectionStore.sortedSections)
const loading = computed(() => sectionStore.loading)

// 加载板块列表
onMounted(async () => {
  await loadSections()
})

async function loadSections() {
  try {
    await sectionStore.fetchSections({ enabled_only: true })
  } catch (error) {
    console.error('加载板块列表失败', error)
  }
}

// 进入板块详情
function goToSection(section) {
  router.push(`/sections/${section.slug}`)
}
</script>

<template>
  <div class="section-list">
    <div class="page-header">
      <h1 class="page-title">论坛板块</h1>
      <p class="page-description">选择您感兴趣的板块，开始探索精彩内容</p>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading">加载中...</div>

    <!-- 板块网格 -->
    <div v-else-if="sections.length > 0" class="sections-grid">
      <div
        v-for="section in sections"
        :key="section.id"
        class="section-card"
        @click="goToSection(section)"
      >
        <div class="section-icon" :style="{ background: section.color + '20', color: section.color }">
          {{ section.icon || '📋' }}
        </div>
        <div class="section-content">
          <h3 class="section-name">{{ section.name }}</h3>
          <p class="section-description">{{ section.description }}</p>
          <div class="section-stats">
            <span class="stat-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
              </svg>
              {{ section.posts_count || 0 }} 帖子
            </span>
          </div>
        </div>
        <div class="section-arrow">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 18 15 12 9 6"></polyline>
          </svg>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <p>暂无可用板块</p>
    </div>
  </div>
</template>

<style scoped>
.section-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 32px;
  text-align: center;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 12px 0;
}

.page-description {
  font-size: 16px;
  color: #6b7280;
  margin: 0;
}

.loading {
  text-align: center;
  padding: 60px 20px;
  color: #6b7280;
  font-size: 16px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #6b7280;
}

.sections-grid {
  display: grid;
  gap: 16px;
}

.section-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
}

.section-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.section-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  flex-shrink: 0;
}

.section-content {
  flex: 1;
  min-width: 0;
}

.section-name {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.section-description {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.5;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.section-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #6b7280;
}

.stat-item svg {
  width: 16px;
  height: 16px;
}

.section-arrow {
  flex-shrink: 0;
  color: #9ca3af;
  transition: transform 0.3s ease;
}

.section-arrow svg {
  width: 24px;
  height: 24px;
}

.section-card:hover .section-arrow {
  transform: translateX(4px);
  color: #1976d2;
}

@media (max-width: 768px) {
  .section-list {
    padding: 16px;
  }

  .page-title {
    font-size: 24px;
  }

  .section-card {
    padding: 16px;
    gap: 12px;
  }

  .section-icon {
    width: 48px;
    height: 48px;
    font-size: 24px;
  }

  .section-arrow {
    display: none;
  }
}
</style>

