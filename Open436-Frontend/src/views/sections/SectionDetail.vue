<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useSectionStore } from '@/stores/modules/section'

const route = useRoute()
const router = useRouter()
const sectionStore = useSectionStore()

const loading = ref(false)
const error = ref(null)

// 计算属性
const section = computed(() => sectionStore.currentSection)

// 加载板块详情
onMounted(async () => {
  await loadSectionDetail()
})

// 监听路由变化
watch(
  () => route.params.idOrSlug,
  () => {
    loadSectionDetail()
  }
)

async function loadSectionDetail() {
  const idOrSlug = route.params.idOrSlug
  if (!idOrSlug) {
    router.push('/sections')
    return
  }

  loading.value = true
  error.value = null

  try {
    await sectionStore.fetchSectionDetail(idOrSlug)
  } catch (err) {
    error.value = '板块不存在或已被删除'
    console.error('加载板块详情失败', err)
  } finally {
    loading.value = false
  }
}

// 返回列表
function goBack() {
  router.push('/sections')
}

// 发帖（TODO: 后续实现）
function goToNewPost() {
  // TODO: 实现发帖功能，预填板块ID
  alert('发帖功能待开发')
}
</script>

<template>
  <div class="section-detail">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading">加载中...</div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-state">
      <p>{{ error }}</p>
      <button class="btn btn-primary" @click="goBack">返回板块列表</button>
    </div>

    <!-- 板块详情 -->
    <div v-else-if="section" class="section-content">
      <!-- 面包屑导航 -->
      <div class="breadcrumb">
        <span class="breadcrumb-link" @click="goBack">论坛板块</span>
        <span class="breadcrumb-separator">/</span>
        <span class="breadcrumb-current">{{ section.name }}</span>
      </div>

      <!-- 板块头部 -->
      <div class="section-header">
        <div class="section-icon" :style="{ background: section.color + '20', color: section.color }">
          {{ section.icon || '📋' }}
        </div>
        <div class="section-info">
          <h1 class="section-name">{{ section.name }}</h1>
          <p class="section-description">{{ section.description }}</p>
          <div class="section-meta">
            <span class="meta-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
              </svg>
              {{ section.posts_count || 0 }} 篇帖子
            </span>
            <span class="meta-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                <line x1="16" y1="2" x2="16" y2="6"></line>
                <line x1="8" y1="2" x2="8" y2="6"></line>
                <line x1="3" y1="10" x2="21" y2="10"></line>
              </svg>
              创建于 {{ new Date(section.created_at).toLocaleDateString() }}
            </span>
          </div>
        </div>
        <button class="btn btn-primary" @click="goToNewPost">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width: 18px; height: 18px; margin-right: 6px">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          发布帖子
        </button>
      </div>

      <!-- 帖子列表 -->
      <div class="posts-section">
        <h2 class="section-title">最新帖子</h2>
        <!-- TODO: 这里将来接入M3内容服务的帖子列表 -->
        <div class="empty-posts">
          <p>暂无帖子，成为第一个发帖的人吧！</p>
          <button class="btn btn-primary" @click="goToNewPost">立即发帖</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.section-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  min-height: 100vh;
}

.loading,
.error-state {
  text-align: center;
  padding: 60px 20px;
}

.loading {
  color: #6b7280;
  font-size: 16px;
}

.error-state p {
  color: #f44336;
  margin-bottom: 20px;
  font-size: 16px;
}

/* 面包屑 */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
  font-size: 14px;
}

.breadcrumb-link {
  color: #1976d2;
  cursor: pointer;
  transition: color 0.2s;
}

.breadcrumb-link:hover {
  color: #1565c0;
  text-decoration: underline;
}

.breadcrumb-separator {
  color: #9ca3af;
}

.breadcrumb-current {
  color: #6b7280;
}

/* 板块头部 */
.section-header {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.section-icon {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  flex-shrink: 0;
}

.section-info {
  flex: 1;
  min-width: 0;
}

.section-name {
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 12px 0;
}

.section-description {
  font-size: 16px;
  color: #6b7280;
  line-height: 1.6;
  margin: 0 0 16px 0;
}

.section-meta {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #6b7280;
}

.meta-item svg {
  width: 16px;
  height: 16px;
}

/* 按钮 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-primary {
  background: #1976d2;
  color: white;
}

.btn-primary:hover {
  background: #1565c0;
}

/* 帖子区域 */
.posts-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #e5e7eb;
}

.empty-posts {
  text-align: center;
  padding: 60px 20px;
}

.empty-posts p {
  color: #6b7280;
  margin-bottom: 20px;
  font-size: 16px;
}

/* 响应式 */
@media (max-width: 768px) {
  .section-detail {
    padding: 16px;
  }

  .section-header {
    flex-direction: column;
    padding: 20px;
  }

  .section-icon {
    width: 64px;
    height: 64px;
    font-size: 32px;
  }

  .section-name {
    font-size: 24px;
  }

  .section-meta {
    flex-direction: column;
    gap: 12px;
  }

  .btn {
    width: 100%;
  }
}
</style>

