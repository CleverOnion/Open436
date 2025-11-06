/**
 * Vue Router 配置
 */
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/modules/user'

// 路由配置
const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: {
      title: '首页',
      requiresAuth: false
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: {
      title: '登录',
      requiresAuth: false
    }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  // 滚动行为
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - ${import.meta.env.VITE_APP_TITLE || 'Open436'}`
  }

  // 获取用户状态
  const userStore = useUserStore()

  // 权限验证逻辑
  if (to.meta.requiresAuth) {
    // 需要登录的页面
    if (!userStore.isLoggedIn) {
      // 未登录，重定向到登录页，并携带原始路径
      next({
        name: 'Login',
        query: { redirect: to.fullPath }
      })
      return
    }
  } else if (to.name === 'Login' && userStore.isLoggedIn) {
    // 已登录用户访问登录页，重定向到首页或原始目标页面
    const redirect = to.query.redirect
    next(redirect ? { path: redirect } : { name: 'Home' })
    return
  }

  next()
})

// 全局后置钩子
router.afterEach(() => {
  // TODO: 可以在这里添加页面访问统计等逻辑
})

export default router

