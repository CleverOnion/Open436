# Store 模块目录

此目录用于存放各个业务模块的 Pinia Store。

## 目录结构

建议按照业务功能进行划分：

```
modules/
├── auth.js      # 认证状态（Token、登录状态）
├── user.js      # 用户信息状态
├── post.js      # 帖子相关状态
├── app.js       # 应用全局状态（主题、语言等）
└── ...
```

## 使用示例

```javascript
// auth.js 示例
import { defineStore } from 'pinia'
import { ref } from 'vue'
import storage from '@/utils/storage'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref(storage.get('token') || '')
  const isLoggedIn = ref(!!token.value)

  // 操作
  function setToken(newToken) {
    token.value = newToken
    isLoggedIn.value = !!newToken
    storage.set('token', newToken)
  }

  function clearToken() {
    token.value = ''
    isLoggedIn.value = false
    storage.remove('token')
  }

  return {
    token,
    isLoggedIn,
    setToken,
    clearToken
  }
})
```

## 使用 Store

```javascript
// 在组件中使用
import { useAuthStore } from '@/stores/modules/auth'

export default {
  setup() {
    const authStore = useAuthStore()
    
    // 访问状态
    console.log(authStore.isLoggedIn)
    
    // 调用操作
    authStore.setToken('xxx')
  }
}
```

## 注意事项

1. 推荐使用 Composition API 风格（Setup 语法）
2. 敏感数据（如 Token）建议配合 localStorage 持久化
3. Store 之间可以相互调用

