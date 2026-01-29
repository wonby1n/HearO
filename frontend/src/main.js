import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'

// TailwindCSS 스타일 임포트
import './assets/main.css'

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)

// Pinia 설정 후 즉시 auth store 초기화 (axios 인터셉터 설정)
const authStore = useAuthStore()
authStore.setupAxiosInterceptors()

app.use(router)

app.mount('#app')
