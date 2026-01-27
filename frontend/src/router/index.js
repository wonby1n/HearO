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
          path: '',
          name: 'client-landing',
          component: () => import('@/views/client/ClientLandingView.vue'),
          meta: {
            title: 'Smart AS Connect',
            role: 'client'
          }
        },
        {
          path: 'call',
          name: 'client-call',
          component: () => import('@/views/client/ClientCallView.vue'),
          meta: {
            title: '고객 상담',
            role: 'client'
          }
        },
        {
          path: 'call-end',
          name: 'client-call-end',
          component: () => import('@/views/client/ClientCallEndView.vue'),
          meta: {
            title: '상담 종료',
            role: 'client'
          }
        },
        {
          path: 'review',
          name: 'client-review',
          component: () => import('@/views/client/ClientReviewView.vue'),
          meta: {
            title: '상담 만족도 조사',
            role: 'client'
          }
        }
      ]
    },
    // 상담원 라우트
    {
      path: '/counselor',
      children: [
        {
          path: 'call',
          name: 'counselor-call',
          component: () => import('@/views/counselor/CounselorCallView.vue'),
          meta: {
            title: '상담원 통화',
            role: 'counselor'
          }
        }
      ]
    },
    // 기존 라우트 (하위 호환성)
    {
      path: '/call',
      redirect: '/counselor/call'
    },
    // 알림 테스트 페이지 (개발용)
    {
      path: '/notification-test',
      name: 'notification-test',
      component: () => import('@/components/notification/NotificationExample.vue'),
      meta: {
        title: '알림 테스트'
      }
    }
  ],
})

// 라우트 가드 - 페이지 타이틀 설정
router.beforeEach((to, from, next) => {
  document.title = to.meta.title || 'HearO'
  next()
})

export default router
