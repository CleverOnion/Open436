<template>
  <div class="login-wrapper">
    <!-- Left Side - Welcome Content -->
    <div ref="loginLeftRef" class="login-left" @mouseenter="handleMouseEnter" @mousemove="handleMouseMove"
      @mouseleave="handleMouseLeave">
      <div ref="gridBackgroundRef" class="grid-background"></div>
      <div ref="mouseGlowRef" class="mouse-glow"></div>
      <div class="welcome-content">
        <h1 class="welcome-title">加入 Open436<br>开启你的技术之旅</h1>
        <p class="welcome-text">一个专注于技术交流与知识分享的社区平台，在这里你可以与志同道合的开发者一起学习、成长。</p>

        <div class="features">
          <div class="feature-item">
            <svg class="feature-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2L2 7l10 5 10-5-10-5z"></path>
              <path d="M2 17l10 5 10-5M2 12l10 5 10-5"></path>
            </svg>
            <span>多样化的技术板块</span>
          </div>
          <div class="feature-item">
            <svg class="feature-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
              <circle cx="9" cy="7" r="4"></circle>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
            </svg>
            <span>活跃的开发者社区</span>
          </div>
          <div class="feature-item">
            <svg class="feature-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline>
            </svg>
            <span>实时的技术动态</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Right Side - Login Form -->
    <div class="login-right">
      <div class="login-container">
        <div class="site-brand">
          <div class="site-logo">O</div>
          <span class="site-name">Open436</span>
        </div>

        <div class="login-header">
          <h1 class="login-title">登录</h1>
          <p class="login-subtitle">使用你的账号登录到 Open436</p>
        </div>

        <n-form ref="formRef" :model="formData" :rules="rules" size="large" @submit.prevent="handleLogin">
          <n-form-item path="username" label="用户名">
            <n-input v-model:value="formData.username" placeholder="输入用户名" :disabled="loading"
              @keyup.enter="handleLogin" />
          </n-form-item>

          <n-form-item path="password" label="密码">
            <n-input v-model:value="formData.password" type="password" placeholder="输入密码" :disabled="loading"
              show-password-on="click" @keyup.enter="handleLogin" />
          </n-form-item>

          <n-form-item>
            <div class="form-options">
              <n-checkbox v-model:checked="formData.remember">记住我</n-checkbox>
              <a href="#" class="forgot-link" @click.prevent="handleForgotPassword">忘记密码？</a>
            </div>
          </n-form-item>

          <n-form-item>
            <n-button type="primary" block :loading="loading" @click="handleLogin">
              登录
            </n-button>
          </n-form-item>
        </n-form>

        <div class="divider">
          <span>或</span>
        </div>

        <div class="register-link">
          还没有账号？<a href="#" @click.prevent="handleRegister">立即注册</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMessage, NForm, NFormItem, NInput, NButton, NCheckbox } from 'naive-ui'
import { useUserStore } from '@/stores/modules/user'
import { authAPI } from '@/api/modules/auth'
import { handleApiError } from '@/utils/errorCode'

console.log('测试')

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const message = useMessage()

// 表单引用
const formRef = ref(null)

// DOM 引用
const loginLeftRef = ref(null)
const gridBackgroundRef = ref(null)
const mouseGlowRef = ref(null)

// 表单数据
const formData = reactive({
  username: '',
  password: '',
  remember: false
})

// 表单验证规则
const rules = {
  username: [
    {
      required: true,
      message: '请输入用户名',
      trigger: ['input', 'blur']
    },
    {
      min: 3,
      max: 20,
      message: '用户名长度在 3 到 20 个字符',
      trigger: ['input', 'blur']
    }
  ],
  password: [
    {
      required: true,
      message: '请输入密码',
      trigger: ['input', 'blur']
    },
    {
      min: 6,
      max: 20,
      message: '密码长度在 6 到 20 个字符',
      trigger: ['input', 'blur']
    }
  ]
}

// 加载状态
const loading = ref(false)

// 鼠标交互处理
const handleMouseEnter = () => {
  if (mouseGlowRef.value) {
    mouseGlowRef.value.style.opacity = '1'
  }
}

const handleMouseMove = (e) => {
  if (!loginLeftRef.value || !gridBackgroundRef.value || !mouseGlowRef.value) return

  const rect = loginLeftRef.value.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top

  // 计算网格移动（最大 30px）
  const moveX = (x / rect.width - 0.5) * 30
  const moveY = (y / rect.height - 0.5) * 30

  // 移动网格背景
  gridBackgroundRef.value.style.transform = `translate(${moveX}px, ${moveY}px) scale(1.02)`

  // 移动光效到鼠标位置
  mouseGlowRef.value.style.left = `${x - 150}px`
  mouseGlowRef.value.style.top = `${y - 150}px`
}

const handleMouseLeave = () => {
  if (gridBackgroundRef.value) {
    gridBackgroundRef.value.style.transform = 'translate(0, 0) scale(1)'
  }
  if (mouseGlowRef.value) {
    mouseGlowRef.value.style.opacity = '0'
  }
}

// 忘记密码处理
const handleForgotPassword = () => {
  message.info('忘记密码功能开发中...')
}

// 注册处理
const handleRegister = () => {
  message.info('注册功能开发中...')
  // 可以跳转到注册页面
  // router.push('/register')
}

// 登录处理
const handleLogin = async () => {
  // 表单验证
  try {
    await formRef.value?.validate()
  } catch (error) {
    return
  }

  loading.value = true

  try {
    // 调用登录接口，包含 rememberMe 参数
    const response = await authAPI.login({
      username: formData.username,
      password: formData.password,
      rememberMe: formData.remember
    })

    // 根据接口文档处理响应格式
    // 响应格式：{ code: 200, message: "登录成功", data: { token, expiresIn, user } }
    if (response.code === 200 && response.data) {
      const { token, expiresIn, user } = response.data

      // 验证必要字段
      if (!token) {
        throw new Error('登录失败：未获取到 token')
      }

      if (!user) {
        throw new Error('登录失败：未获取到用户信息')
      }

      // 存储登录信息到 store
      userStore.login(token, user, expiresIn || 2592000)

      // 显示成功消息
      message.success(response.message || '登录成功')

      // 登录成功后跳转
      const redirect = route.query.redirect || '/'

      // 延迟跳转，让用户看到成功提示
      setTimeout(() => {
        router.push(redirect)
      }, 500)
    } else {
      // 响应格式不符合预期
      throw new Error(response.message || '登录失败，响应格式错误')
    }
  } catch (error) {
    // 使用错误处理工具函数获取友好的错误信息
    const errorMessage = handleApiError(error)
    console.error('登录失败：', error)
    message.error(errorMessage)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 组件挂载后的初始化
})

onUnmounted(() => {
  // 清理工作
})
</script>

<style scoped>
/* CSS Variables - 使用原型设计变量 */
.login-wrapper {
  --primary: #1976D2;
  --primary-dark: #1565C0;
  --text-primary: #212121;
  --text-secondary: #757575;
  --divider: #E0E0E0;
  --background: #FFFFFF;
  --background-secondary: #FAFAFA;
  --space-xs: 4px;
  --space-sm: 8px;
  --space-md: 12px;
  --space-base: 16px;
  --space-lg: 24px;
  --space-xl: 32px;
  --space-2xl: 48px;
  --radius-sm: 4px;
  --transition-fast: 200ms cubic-bezier(0.4, 0.0, 0.2, 1);
}

.login-wrapper {
  display: flex;
  min-height: 100vh;
}

/* Left Side - Welcome Content */
.login-left {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-xl);
  background: var(--background);
  position: relative;
  overflow: hidden;
}

.grid-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background:
    linear-gradient(rgba(25, 118, 210, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(25, 118, 210, 0.06) 1px, transparent 1px);
  background-size: 20px 20px;
  transition: transform 0.3s ease-out, opacity 0.3s ease-out;
}

.mouse-glow {
  position: absolute;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(25, 118, 210, 0.15) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.3s ease-out;
  z-index: 2;
}

.login-left::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(25, 118, 210, 0.08) 0%, transparent 70%);
  border-radius: 50%;
}

.login-left::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: -10%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(25, 118, 210, 0.05) 0%, transparent 70%);
  border-radius: 50%;
}

.welcome-content {
  max-width: 480px;
  position: relative;
  z-index: 1;
}

.welcome-title {
  font-size: 36px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-base);
  line-height: 1.2;
}

.welcome-text {
  font-size: 16px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: var(--space-xl);
}

.features {
  display: flex;
  flex-direction: column;
  gap: var(--space-base);
}

.feature-item {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  color: var(--text-secondary);
  font-size: 15px;
}

.feature-icon {
  width: 20px;
  height: 20px;
  color: var(--primary);
  flex-shrink: 0;
}

/* Right Side - Login Form */
.login-right {
  flex: 1;
  background: var(--background-secondary);
  padding: var(--space-xl);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-container {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
}

.site-brand {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin-bottom: var(--space-lg);
}

.site-logo {
  width: 40px;
  height: 40px;
  background: var(--primary);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  font-weight: 700;
}

.site-name {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.login-header {
  margin-bottom: var(--space-lg);
}

.login-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-xs);
  margin-top: 0;
}

.login-subtitle {
  color: var(--text-secondary);
  font-size: 15px;
  margin: 0;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  font-size: 14px;
}

.forgot-link {
  color: var(--primary);
  text-decoration: none;
  font-size: 14px;
}

.forgot-link:hover {
  text-decoration: underline;
}

.divider {
  display: flex;
  align-items: center;
  margin: var(--space-lg) 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--divider);
}

.divider::before {
  margin-right: var(--space-base);
}

.divider::after {
  margin-left: var(--space-base);
}

.divider span {
  flex-shrink: 0;
}

.register-link {
  text-align: center;
  color: var(--text-secondary);
  font-size: 14px;
}

.register-link a {
  color: var(--primary);
  text-decoration: none;
  font-weight: 500;
}

.register-link a:hover {
  text-decoration: underline;
}

/* 调整 Naive UI 表单项间距，使其更紧凑 */
:deep(.n-form-item) {
  margin-bottom: 4px !important;
}

/* 进一步减小表单项内部的间距 */
:deep(.n-form-item__label) {
  margin-bottom: 4px !important;
  padding-bottom: 0 !important;
}

/* 减小表单项之间的额外间距 */
:deep(.n-form-item + .n-form-item) {
  margin-top: 0 !important;
}

/* 输入框表单项特别处理 - 用户名和密码之间更紧凑 */
:deep(.n-form-item .n-input) {
  margin-top: 0 !important;
}

/* 记住我选项区域 - 密码和记住我之间更紧凑 */
:deep(.n-form-item:has(.form-options)) {
  margin-top: 4px !important;
  margin-bottom: 8px !important;
}

/* 登录按钮区域保持稍大间距 */
:deep(.n-form-item:last-child) {
  margin-top: 8px !important;
  margin-bottom: var(--space-base) !important;
}

/* Responsive */
@media (max-width: 959px) {
  .login-wrapper {
    flex-direction: column;
  }

  .login-left {
    display: none;
  }

  .login-right {
    padding: var(--space-lg);
  }

  .welcome-title {
    font-size: 28px;
  }
}
</style>
