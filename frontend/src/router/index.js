import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/call'
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
