import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: () => import('@/views/dashboard/DashboardView.vue'),
      meta: {
        title: 'Dashboard'
      }
    },
    {
      path: '/call',
      name: 'call',
      component: () => import('@/views/call/CallView.vue'),
      meta: {
        title: '통화 중'
      }
    }
  ],
})

export default router
