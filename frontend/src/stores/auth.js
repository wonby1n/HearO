import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
  // 상태
  const accessToken = ref(localStorage.getItem('accessToken') || null)
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const isLoading = ref(false)
  const error = ref(null)

  // 인터셉터 ID 저장 (중복 등록 방지)
  let requestInterceptorId = null
  let responseInterceptorId = null

  // Getters
  const isAuthenticated = computed(() => !!accessToken.value)
  const getUser = computed(() => user.value)
  const getToken = computed(() => accessToken.value)

  // axios 인터셉터 설정
  const setupAxiosInterceptors = () => {
    // 기존 인터셉터 제거 (중복 방지)
    if (requestInterceptorId !== null) {
      axios.interceptors.request.eject(requestInterceptorId)
    }
    if (responseInterceptorId !== null) {
      axios.interceptors.response.eject(responseInterceptorId)
    }

    // 요청 인터셉터 등록
    requestInterceptorId = axios.interceptors.request.use(
      (config) => {
        const token = accessToken.value
        console.log('[Axios Interceptor] Request to:', config.url)
        console.log('[Axios Interceptor] Token exists:', !!token)

        if (token) {
          config.headers.Authorization = `Bearer ${token}`
          console.log('[Axios Interceptor] Added Authorization header')
        }
        return config
      },
      (error) => {
        console.error('[Axios Interceptor] Request error:', error)
        return Promise.reject(error)
      }
    )

    // 응답 인터셉터 등록
    responseInterceptorId = axios.interceptors.response.use(
      (response) => response,
      (error) => {
        console.error('[Axios Interceptor] Response error:', error.response?.status, error.config?.url)

        if (error.response?.status === 401) {
          // 로그인/리프레시 요청이 아닌 경우에만 로그아웃
          const isAuthEndpoint = error.config?.url?.includes('/api/v1/auth/')
          if (!isAuthEndpoint) {
            console.warn('[Axios Interceptor] 401 Unauthorized - logging out')
            logout()
          }
        }
        return Promise.reject(error)
      }
    )
  }

  // 로그인
  const login = async (email, password) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await axios.post('/api/v1/auth/login', {
        email,
        password
      })

      const { accessToken: token, userId, username, userRole } = response.data

      // 사용자 정보 객체 구성
      const userData = {
        id: userId,
        name: username,
        role: userRole
      }

      // 토큰 및 사용자 정보 저장
      accessToken.value = token
      user.value = userData
      
      localStorage.setItem('accessToken', token)
      localStorage.setItem('user', JSON.stringify(userData))

      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.message || '로그인에 실패했습니다.'
      return { success: false, error: error.value }
    } finally {
      isLoading.value = false
    }
  }

  // 로그아웃
  const logout = async () => {
    try {
      // 백엔드 로그아웃 API 호출 (refreshToken 쿠키 삭제)
      await axios.post('/api/v1/auth/logout')
    } catch (err) {
      console.error('로그아웃 API 호출 실패:', err)
    }

    // 프론트엔드 상태 및 localStorage 정리
    accessToken.value = null
    user.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('user')

    // 로그인 페이지로 리다이렉트
    router.push('/login')
  }

  // 토큰 갱신
  const refreshToken = async () => {
    try {
      const response = await axios.post('/api/v1/auth/refresh')
      const { accessToken: token } = response.data

      accessToken.value = token
      localStorage.setItem('accessToken', token)

      return { success: true }
    } catch (err) {
      logout()
      return { success: false }
    }
  }

  // 토큰 직접 설정 (외부에서 받은 토큰 사용 시)
  const setToken = (token, userData = null) => {
    accessToken.value = token
    localStorage.setItem('accessToken', token)

    if (userData) {
      user.value = userData
      localStorage.setItem('user', JSON.stringify(userData))
    }
  }

  return {
    // State
    accessToken,
    user,
    isLoading,
    error,

    // Getters
    isAuthenticated,
    getUser,
    getToken,

    // Actions
    login,
    logout,
    refreshToken,
    setToken,
    setupAxiosInterceptors
  }
})
