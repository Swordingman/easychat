import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

// 1. 完整引入 Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css' // 引入样式
import * as ElementPlusIconsVue from '@element-plus/icons-vue' // 引入图标

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
const app = createApp(App)

// 2. 将所有图标注册为全局组件
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
// 3. 使用 Element Plus 插件
app.use(ElementPlus)

app.mount('#app')
