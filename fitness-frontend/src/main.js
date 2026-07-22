import { createApp } from 'vue'
import App from './App.vue'
import { createPinia } from 'pinia'
import router from './router.js'
import { setupRequestRouter } from './utils/request.js'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// 初始化请求模块的 router
setupRequestRouter(router)

app.mount('#app')