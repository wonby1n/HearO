import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

export const useAuthStore = defineStore('auth', () => {
  // 상태
  const accessToken = ref(localStorage.getItem('accessToken') || null)
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const isLoading = ref(false)
  const error = ref(null)

  // Getters
  const isAuthenticated = computed(() => !!accessToken.value)
  const getUser = computed(() => user.value)
  const getToken = computed(() => accessToken.value)

  // axios 인터셉터 설정
  const setupAxiosInterceptors = () => {
    axios.interceptors.request.use(
      (config) => {
        if (accessToken.value) {
          config.headers.Authorization = `Bearer ${accessToken.value}`
        }
        return config
      },
      (error) => Promise.reject(error)
    )

    axios.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          // 토큰 만료 시 로그아웃
          logout()
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
      const response = await axios.post('/api/auth/login', {
        email,
        password
      })

      const { token, user: userData } = response.data

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
  const logout = () => {
    accessToken.value = null
    user.value = null

    localStorage.removeItem('accessToken')
    localStorage.removeItem('user')
  }

  // 토큰 갱신
  const refreshToken = async () => {
    try {
      const response = await axios.post('/api/auth/refresh')
      const { token } = response.data

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

  // 초기화 시 인터셉터 설정
  setupAxiosInterceptors()

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
