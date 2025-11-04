/**
 * Pinia 状态管理配置
 */
import { createPinia } from 'pinia'

const pinia = createPinia()

// TODO: 可以添加 Pinia 插件
// 示例：持久化插件
// import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
// pinia.use(piniaPluginPersistedstate)

export default pinia

