import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 루트 경로 - 로그인 페이지로 리다이렉트
    {
      path: '/',
      redirect: '/login'
    },
    // 로그인 페이지
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: {
        title: '로그인',
        requiresAuth: false
      }
    },
    // 대시보드
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('@/views/dashboard/DashboardView.vue'),
      meta: {
        title: 'Dashboard',
        requiresAuth: true
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
          path: 'waiting',
          name: 'client-waiting',
          component: () => import('@/views/client/ClientWaitingView.vue'),
          meta: {
            title: '상담 대기',
            role: 'client'
          }
        },
        {
          path: 'consultation/verification',
          name: 'client-consultation-verification',
          component: () => import('@/views/client/ClientConsultationVerificationView.vue'),
          meta: {
            title: '본인 확인',
            role: 'client'
          }
        },
        {
          path: 'consultation/consent',
          name: 'client-consultation-consent',
          component: () => import('@/views/client/ClientConsultationConsentView.vue'),
          meta: {
            title: '약관 동의',
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
        },
        {
          path: 'final',
          name: 'client-final',
          component: () => import('@/views/client/ClientFinalView.vue'),
          meta: {
            title: '상담 완료',
            role: 'client'
          }
        },
        {
          path: 'reconnect',
          name: 'client-reconnect',
          component: () => import('@/views/client/ClientReconnectView.vue'),
          meta: {
            title: '상담 재연결',
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
    },
    // 휴식 권장 모달 테스트 페이지 (개발용)
    {
      path: '/rest-recommend-test',
      name: 'rest-recommend-test',
      component: () => import('@/components/alert/RestRecommendModalExample.vue'),
      meta: {
        title: '휴식 권장 모달 테스트'
      }
    }
  ],
})

// 라우트 가드 - 인증 체크 및 페이지 타이틀 설정
router.beforeEach((to, from, next) => {
  // 페이지 타이틀 설정
  document.title = to.meta.title || 'HearO'

  // 인증이 필요한 페이지 체크
  const token = localStorage.getItem('accessToken')
  const requiresAuth = to.meta.requiresAuth

  if (requiresAuth && !token) {
    // 인증 필요한데 토큰 없으면 로그인 페이지로
    console.log('[Router Guard] 인증 필요 - 로그인 페이지로 리다이렉트')
    next({ name: 'login', query: { redirect: to.fullPath } })
  } else if (to.name === 'login' && token) {
    // 이미 로그인된 상태에서 로그인 페이지 접근 시 대시보드로
    console.log('[Router Guard] 이미 로그인됨 - 대시보드로 리다이렉트')
    next({ name: 'dashboard' })
  } else {
    next()
  }
})

export default router