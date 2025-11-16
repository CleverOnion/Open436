<script setup>
import { ref, onMounted, computed } from 'vue'
import { useSectionStore } from '@/stores/modules/section'

const sectionStore = useSectionStore()

// 对话框控制
const dialogVisible = ref(false)
const dialogTitle = ref('添加板块')
const isEditing = ref(false)
const confirmDialogVisible = ref(false)
const sectionToDelete = ref(null)

// 消息提示
const message = ref({ show: false, text: '', type: 'success' })

// 表单数据
const formData = ref({
  slug: '',
  name: '',
  description: '',
  color: '#1976D2',
  icon: '📋', // 临时使用 emoji，未来集成 M7 后使用 icon_file_id
  icon_file_id: null, // 图标文件ID（M7文件服务）
  sort_order: 1,
  is_enabled: true
})

// 预设图标（临时方案：使用 emoji）
// TODO: 集成 M7 文件服务后，改为图片上传
const iconOptions = ['💻', '🎨', '💬', '❓', '📦', '📢', '🎯', '⚡', '🔥', '⭐', '📋', '🏆']

// 计算属性
const sections = computed(() => sectionStore.sortedSections)
const loading = computed(() => sectionStore.loading)

// 加载板块列表
onMounted(async () => {
  await loadSections()
})

async function loadSections() {
  try {
    await sectionStore.fetchSections({ enabled_only: false })
  } catch (error) {
    showMessage('加载板块列表失败', 'error')
  }
}

// 显示消息
function showMessage(text, type = 'success') {
  message.value = { show: true, text, type }
  setTimeout(() => {
    message.value.show = false
  }, 3000)
}

// 打开添加对话框
function handleAdd() {
  isEditing.value = false
  dialogTitle.value = '添加板块'
  formData.value = {
    slug: '',
    name: '',
    description: '',
    color: '#1976D2',
    icon: '📋',
    sort_order: sections.value.length + 1,
    is_enabled: true
  }
  dialogVisible.value = true
}

// 打开编辑对话框
function handleEdit(section) {
  isEditing.value = true
  dialogTitle.value = '编辑板块'
  formData.value = {
    id: section.id,
    slug: section.slug,
    name: section.name,
    description: section.description,
    color: section.color,
    icon: section.icon || '📋',
    sort_order: section.sort_order,
    is_enabled: section.is_enabled
  }
  dialogVisible.value = true
}

// 提交表单
async function handleSubmit() {
  // 简单验证
  if (!formData.value.slug || !formData.value.name) {
    showMessage('请填写必填项', 'error')
    return
  }

  try {
    // 准备提交数据
    const submitData = { ...formData.value }
    
    // 注意：当前使用 emoji 作为临时方案，后端可能不支持 icon 字段
    // 未来集成 M7 后，应该上传图片获取 icon_file_id
    // 如果后端不支持 icon 字段，可以删除它
    if (!submitData.icon_file_id) {
      delete submitData.icon_file_id // 如果没有文件ID，删除该字段
    }
    
    if (isEditing.value) {
      await sectionStore.updateSection(submitData.id, submitData)
      showMessage('板块更新成功', 'success')
    } else {
      await sectionStore.createSection(submitData)
      showMessage('板块创建成功', 'success')
    }
    dialogVisible.value = false
    await loadSections()
  } catch (error) {
    showMessage(error.response?.data?.message || '操作失败', 'error')
  }
}

// 切换启用状态
async function handleToggleStatus(section) {
  try {
    const newStatus = !section.is_enabled
    await sectionStore.toggleSectionStatus(section.id, newStatus)
    showMessage(`板块已${newStatus ? '启用' : '禁用'}`, 'success')
    await loadSections()
  } catch (error) {
    showMessage('操作失败', 'error')
  }
}

// 删除板块
function handleDelete(section) {
  sectionToDelete.value = section
  confirmDialogVisible.value = true
}

async function confirmDelete() {
  try {
    await sectionStore.deleteSection(sectionToDelete.value.id, false)
    showMessage('删除成功', 'success')
    confirmDialogVisible.value = false
    sectionToDelete.value = null
    await loadSections()
  } catch (error) {
    showMessage('删除失败', 'error')
  }
}
</script>

<template>
  <div class="section-manage">
    <!-- 消息提示 -->
    <div v-if="message.show" class="message" :class="'message-' + message.type">
      {{ message.text }}
    </div>

    <!-- 页面头部 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">板块管理</h1>
        <p class="page-description">管理论坛板块分类和设置</p>
      </div>
      <button class="btn btn-primary" @click="handleAdd">
        <svg
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          style="width: 18px; height: 18px; margin-right: 6px"
        >
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
        添加板块
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading">加载中...</div>

    <!-- 板块卡片网格 -->
    <div v-else-if="sections.length > 0" class="sections-grid">
      <div
        v-for="section in sections"
        :key="section.id"
        class="section-card"
        :class="{ disabled: !section.is_enabled }"
      >
        <!-- 板块头部 -->
        <div class="section-header">
          <div class="section-icon" :style="{ background: section.color + '20', color: section.color }">
            {{ section.icon || '📋' }}
          </div>
          <div class="section-info">
            <div class="section-name">{{ section.name }}</div>
            <div class="section-slug">/{{ section.slug }}</div>
          </div>
        </div>

        <!-- 板块描述 -->
        <p class="section-description">{{ section.description }}</p>

        <!-- 统计数据 -->
        <div class="section-stats">
          <div class="stat-item">
            <div class="stat-value">{{ section.posts_count || 0 }}</div>
            <div class="stat-label">帖子数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ section.sort_order }}</div>
            <div class="stat-label">排序</div>
          </div>
          <div class="stat-item">
            <span class="status-badge" :class="section.is_enabled ? 'status-enabled' : 'status-disabled'">
              {{ section.is_enabled ? '启用' : '禁用' }}
            </span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="section-actions">
          <button class="btn btn-small" @click="handleEdit(section)">编辑</button>
          <button class="btn btn-small" @click="handleToggleStatus(section)">
            {{ section.is_enabled ? '禁用' : '启用' }}
          </button>
          <button class="btn btn-small btn-danger" @click="handleDelete(section)">删除</button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <p>暂无板块数据</p>
      <button class="btn btn-primary" @click="handleAdd">添加第一个板块</button>
    </div>

    <!-- 编辑对话框 -->
    <div v-if="dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
      <div class="dialog">
        <div class="dialog-header">
          <h2>{{ dialogTitle }}</h2>
          <button class="dialog-close" @click="dialogVisible = false">&times;</button>
        </div>

        <div class="dialog-body">
          <div class="form-group">
            <label class="form-label required">板块标识</label>
            <input
              v-model="formData.slug"
              type="text"
              class="form-input"
              placeholder="小写字母、数字、下划线，3-20字符"
              :disabled="isEditing"
            />
            <div class="form-tip">板块唯一标识，创建后不可修改</div>
          </div>

          <div class="form-group">
            <label class="form-label required">板块名称</label>
            <input v-model="formData.name" type="text" class="form-input" placeholder="请输入板块名称" maxlength="50" />
          </div>

          <div class="form-group">
            <label class="form-label">板块描述</label>
            <textarea
              v-model="formData.description"
              class="form-textarea"
              placeholder="请输入板块描述"
              rows="3"
              maxlength="500"
            ></textarea>
          </div>

          <div class="form-group">
            <label class="form-label">板块图标</label>
            <div class="icon-selector">
              <span
                v-for="icon in iconOptions"
                :key="icon"
                class="icon-option"
                :class="{ selected: formData.icon === icon }"
                @click="formData.icon = icon"
              >
                {{ icon }}
              </span>
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">板块颜色</label>
            <div class="color-input-group">
              <input v-model="formData.color" type="color" class="form-color" />
              <input v-model="formData.color" type="text" class="form-input" placeholder="#1976D2" />
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">排序号</label>
            <input v-model.number="formData.sort_order" type="number" class="form-input" min="1" max="999" />
            <div class="form-tip">数字越小越靠前</div>
          </div>

          <div class="form-group">
            <label class="form-label">启用状态</label>
            <label class="switch">
              <input v-model="formData.is_enabled" type="checkbox" />
              <span class="slider"></span>
            </label>
            <span style="margin-left: 10px">{{ formData.is_enabled ? '启用' : '禁用' }}</span>
          </div>
        </div>

        <div class="dialog-footer">
          <button class="btn" @click="dialogVisible = false">取消</button>
          <button class="btn btn-primary" @click="handleSubmit">确定</button>
        </div>
      </div>
    </div>

    <!-- 确认删除对话框 -->
    <div v-if="confirmDialogVisible" class="dialog-overlay" @click.self="confirmDialogVisible = false">
      <div class="dialog dialog-small">
        <div class="dialog-header">
          <h2>确认删除</h2>
          <button class="dialog-close" @click="confirmDialogVisible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <p>确定要删除板块"{{ sectionToDelete?.name }}"吗？删除后无法恢复。</p>
        </div>
        <div class="dialog-footer">
          <button class="btn" @click="confirmDialogVisible = false">取消</button>
          <button class="btn btn-danger" @click="confirmDelete">确定删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 基础变量 */
:root {
  --primary: #1976d2;
  --primary-hover: #1565c0;
  --danger: #f44336;
  --danger-hover: #d32f2f;
  --success: #4caf50;
  --text-primary: #1f2937;
  --text-secondary: #6b7280;
  --border: #e5e7eb;
  --background: #f9fafb;
  --white: #ffffff;
}

.section-manage {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  background: var(--background);
  min-height: 100vh;
}

/* 消息提示 */
.message {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 12px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  animation: slideIn 0.3s ease;
}

.message-success {
  background: var(--success);
  color: white;
}

.message-error {
  background: var(--danger);
  color: white;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.page-description {
  color: var(--text-secondary);
  margin: 0;
}

/* 按钮样式 */
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
  background: var(--white);
  color: var(--text-primary);
  border: 1px solid var(--border);
}

.btn:hover {
  background: var(--background);
}

.btn-primary {
  background: var(--primary);
  color: white;
  border: none;
}

.btn-primary:hover {
  background: var(--primary-hover);
}

.btn-danger {
  background: var(--danger);
  color: white;
  border: none;
}

.btn-danger:hover {
  background: var(--danger-hover);
}

.btn-small {
  padding: 6px 12px;
  font-size: 13px;
}

/* 加载状态 */
.loading {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
  font-size: 16px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-state p {
  color: var(--text-secondary);
  margin-bottom: 20px;
}

/* 板块卡片网格 */
.sections-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.section-card {
  background: var(--white);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.section-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.section-card.disabled {
  opacity: 0.6;
  background: var(--background);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.section-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.section-info {
  flex: 1;
  min-width: 0;
}

.section-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.section-slug {
  font-size: 14px;
  color: var(--text-secondary);
  font-family: 'Courier New', monospace;
}

.section-description {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.5;
  margin: 0 0 16px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.section-stats {
  display: flex;
  gap: 16px;
  padding: 12px 0;
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
  margin-bottom: 16px;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #9ca3af;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-enabled {
  background: #d4edda;
  color: #155724;
}

.status-disabled {
  background: #f8d7da;
  color: #721c24;
}

.section-actions {
  display: flex;
  gap: 8px;
}

.section-actions .btn {
  flex: 1;
}

/* 对话框 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.dialog {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.dialog-small {
  max-width: 400px;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid var(--border);
}

.dialog-header h2 {
  margin: 0;
  font-size: 20px;
  color: var(--text-primary);
}

.dialog-close {
  background: none;
  border: none;
  font-size: 28px;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
}

.dialog-close:hover {
  background: var(--background);
}

.dialog-body {
  padding: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px;
  border-top: 1px solid var(--border);
}

/* 表单 */
.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: var(--text-primary);
}

.form-label.required::after {
  content: '*';
  color: var(--danger);
  margin-left: 4px;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border);
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: var(--primary);
}

.form-input:disabled {
  background: var(--background);
  cursor: not-allowed;
}

.form-textarea {
  resize: vertical;
  font-family: inherit;
}

.form-tip {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

/* 图标选择器 */
.icon-selector {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(48px, 1fr));
  gap: 8px;
}

.icon-option {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  border: 2px solid var(--border);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.icon-option:hover {
  border-color: var(--primary);
  background: rgba(25, 118, 210, 0.1);
}

.icon-option.selected {
  border-color: var(--primary);
  background: rgba(25, 118, 210, 0.1);
}

/* 颜色输入 */
.color-input-group {
  display: flex;
  gap: 12px;
  align-items: center;
}

.form-color {
  width: 60px;
  height: 40px;
  border: 1px solid var(--border);
  border-radius: 6px;
  cursor: pointer;
}

.color-input-group .form-input {
  flex: 1;
}

/* 开关 */
.switch {
  position: relative;
  display: inline-block;
  width: 48px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: 0.3s;
  border-radius: 24px;
}

.slider:before {
  position: absolute;
  content: '';
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.3s;
  border-radius: 50%;
}

input:checked + .slider {
  background-color: var(--primary);
}

input:checked + .slider:before {
  transform: translateX(24px);
}

/* 响应式 */
@media (max-width: 768px) {
  .section-manage {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .sections-grid {
    grid-template-columns: 1fr;
  }

  .dialog {
    max-width: 100%;
    border-radius: 0;
  }
}
</style>

