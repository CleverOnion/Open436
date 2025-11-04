/**
 * Vue Router 配置
 */
import { createRouter, createWebHistory } from 'vue-router'

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
  }
  // TODO: 添加更多路由配置
  // 示例：
  // {
  //   path: '/login',
  //   name: 'Login',
  //   component: () => import('@/views/Login.vue'),
  //   meta: { title: '登录', requiresAuth: false }
  // },
  // {
  //   path: '/profile',
  //   name: 'Profile',
  //   component: () => import('@/views/Profile.vue'),
  //   meta: { title: '个人中心', requiresAuth: true }
  // }
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

  // TODO: 添加权限验证逻辑
  // 示例：
  // if (to.meta.requiresAuth) {
  //   const token = storage.get('token')
  //   if (!token) {
  //     next({ name: 'Login', query: { redirect: to.fullPath } })
  //     return
  //   }
  // }

  next()
})

// 全局后置钩子
router.afterEach(() => {
  // TODO: 可以在这里添加页面访问统计等逻辑
})

export default router

