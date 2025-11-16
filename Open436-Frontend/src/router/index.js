/**
 * Vue Router 配置
 */
import { createRouter, createWebHistory } from 'vue-router'
import storage from '@/utils/storage'

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
  
  // ========== M5 板块管理模块路由 ==========
  {
    path: '/sections',
    name: 'SectionList',
    component: () => import('@/views/sections/SectionList.vue'),
    meta: {
      title: '板块列表',
      requiresAuth: false
    }
  },
  {
    path: '/sections/:idOrSlug',
    name: 'SectionDetail',
    component: () => import('@/views/sections/SectionDetail.vue'),
    meta: {
      title: '板块详情',
      requiresAuth: false
    }
  },
  {
    path: '/admin/sections',
    name: 'SectionManage',
    component: () => import('@/views/sections/SectionManage.vue'),
    meta: {
      title: '板块管理',
      requiresAuth: true,
      requiresAdmin: true
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

  if (to.meta && to.meta.requiresAuth) {
    const token = storage.get('token')
    if (!token) {
      next({ name: 'Home' })
      return
    }
  }

  if (to.meta && to.meta.requiresAdmin) {
    const userInfo = storage.get('user_info') || {}
    const role = userInfo && userInfo.role
    if (role !== 'admin') {
      next({ name: 'Home' })
      return
    }
  }

  next()
})

// 全局后置钩子
router.afterEach(() => {
  // TODO: 可以在这里添加页面访问统计等逻辑
})

export default router

