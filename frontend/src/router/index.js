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
      },
    },
    // 고객 라우트
    {
      path: '/client',
      children: [
        {
          path: 'call',
          name: 'client-call',
          component: () => import('@/views/client/ClientCallView.vue'),
          meta: {
            title: '고객 상담',
            role: 'client'
          }
        }
      ]
    },
    // 기존 라우트 (하위 호환성)
    {
      path: '/call',
      redirect: '/counselor/call'
    }
  ],
})

// 라우트 가드 - 페이지 타이틀 설정
router.beforeEach((to, from, next) => {
  document.title = to.meta.title || 'HearO'
  next()
})

export default router
