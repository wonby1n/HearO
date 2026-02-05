import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'

// TailwindCSS 스타일 임포트
import './assets/main.css'

// vue3-grid-layout-next 스타일 임포트
import 'vue3-grid-layout-next/dist/style.css'

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)

// Pinia 설정 후 즉시 auth store 초기화 (axios 인터셉터 설정)
const authStore = useAuthStore()
authStore.setupAxiosInterceptors()

app.use(router)

app.mount('#app')

// 모바일 디버그: URL에 ?debug=true 파라미터가 있으면 eruda 로드
if (new URLSearchParams(window.location.search).has('debug')) {
  const script = document.createElement('script')
  script.src = 'https://cdn.jsdelivr.net/npm/eruda@latest/dist/eruda.min.js'
  script.onload = () => window.eruda?.init()
  document.head.appendChild(script)
}
