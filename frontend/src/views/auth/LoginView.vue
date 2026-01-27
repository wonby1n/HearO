<template>
  <div class="login-page">
    <!-- 배경 디자인 요소: 큰 도넛 형태 -->
    <div class="background-wrapper">
      <div class="bg-donut"></div>
      <div class="bg-circle"></div>
    </div>

    <!-- 로그인 폼 컨테이너 -->
    <div class="login-container">
      <!-- 로고 섹션 -->
      <div class="logo">
        <span class="logo-text">Hear</span><span class="logo-o">O</span>
      </div>

      <h1 class="welcome-title">Welcome!</h1>
      <p class="welcome-subtitle">login your account</p>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <input
            id="email"
            v-model="email"
            type="email"
            placeholder="Email"
            required
            :disabled="isLoading"
          />
        </div>

        <div class="form-group">
          <input
            id="password"
            v-model="password"
            type="password"
            placeholder="Password"
            required
            :disabled="isLoading"
          />
        </div>

        <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

        <button
          type="submit"
          class="login-btn"
          :disabled="isLoading"
        >
          {{ isLoading ? 'Logging in...' : 'LOG-IN' }}
        </button>

        <div class="form-links">
          <a href="#" class="link" @click.prevent="handleForgotPassword">Forgot Password?</a>
        </div>
      </form>

      <!-- 하단 정보 -->
      <div class="footer-info">
        <p>A professional consultant can help you with your solution.</p>
        <p>support@hearo.io / Tel: 000-0000-0000</p>
      </div>

      <p class="copyright">© HearO</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const errorMessage = ref('')
const isLoading = ref(false)

const handleLogin = async () => {
  errorMessage.value = ''
  isLoading.value = true

  try {
    const result = await authStore.login(email.value, password.value)

    if (result.success) {
      const redirect = router.currentRoute.value.query.redirect || '/'
      router.push(redirect)
    } else {
      errorMessage.value = result.error
    }
  } catch (error) {
    errorMessage.value = '로그인 중 오류가 발생했습니다.'
  } finally {
    isLoading.value = false
  }
}

const handleForgotPassword = () => {
  alert('비밀번호 찾기 기능은 준비 중입니다.')
}
</script>

<style scoped>
/* 전체 페이지 설정 - Dashboard와 동일한 배경 */
.login-page {
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f9fafb; /* gray-50 - Dashboard 배경과 동일 */
  position: relative;
  overflow: hidden;
  /* 폰트는 전역 설정(main.css) 상속 */
}

/* 배경 레이어 컨테이너 */
.background-wrapper {
  position: absolute;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

/* 거대한 남색 도넛 배경 - primary 색상 사용 */
.bg-donut {
  position: absolute;
  width: 1200px;
  height: 1200px;
  background: rgba(31, 58, 140, 0.08); /* primary-600 #1F3A8C with opacity */
  border-radius: 50%;
  z-index: 1;
}

/* 중앙의 흰색 원 (도넛 구멍 역할) */
.bg-circle {
  position: absolute;
  width: 650px;
  height: 650px;
  background: #ffffff;
  border-radius: 50%;
  box-shadow: 0 10px 50px rgba(0, 0, 0, 0.03);
  z-index: 2;
}

/* 로그인 컨테이너 */
.login-container {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 400px;
  padding: 40px 20px;
  text-align: center;
}

/* 로고 스타일링 - primary-600 색상 사용 */
.logo {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  letter-spacing: -1px;
}

.logo-text {
  color: #111827; /* gray-900 */
}

.logo-o {
  color: #1F3A8C; /* primary-600 - 피그마 메인 컬러 */
  position: relative;
}

/* 타이틀 및 서브타이틀 */
.welcome-title {
  font-size: 32px;
  font-weight: 300;
  color: #111827; /* gray-900 */
  margin: 0 0 12px 0;
}

.welcome-subtitle {
  font-size: 14px;
  color: #374151; /* gray-700 */
  font-weight: 600;
  margin-bottom: 35px;
}

/* 로그인 폼 */
.login-form {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-group input {
  width: 100%;
  padding: 16px 20px;
  border: 1.5px solid #c7d2fe; /* primary-200 */
  border-radius: 8px; /* Dashboard와 동일한 rounded-lg */
  font-size: 14px;
  color: #111827;
  outline: none;
  transition: all 0.2s;
  box-sizing: border-box;
  background: #ffffff;
}

.form-group input::placeholder {
  color: #9ca3af; /* gray-400 */
}

.form-group input:focus {
  border-color: #1F3A8C; /* primary-600 */
  box-shadow: 0 0 0 3px rgba(31, 58, 140, 0.1); /* primary-600 with opacity */
}

.form-group input:disabled {
  background: #f3f4f6; /* gray-100 */
  cursor: not-allowed;
}

.error-message {
  color: #ef4444; /* danger */
  font-size: 13px;
  margin: 0;
  padding: 12px;
  background: #fee2e2; /* danger-light */
  border-radius: 8px;
  text-align: left;
}

.login-btn {
  width: 100%;
  padding: 16px;
  background-color: #1F3A8C; /* primary-600 - 피그마 메인 컬러 */
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 10px;
  transition: all 0.2s;
}

.login-btn:hover:not(:disabled) {
  background-color: #1a3278; /* primary-700 */
}

.login-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 링크 섹션 */
.form-links {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 25px;
}

.link {
  font-size: 13px;
  text-decoration: none;
  color: #374151; /* gray-700 */
  font-weight: 600;
  transition: color 0.2s;
}

.link:hover {
  color: #1F3A8C; /* primary-600 */
}

/* 푸터 정보 */
.footer-info {
  margin-top: 60px;
  font-size: 12px;
  color: #9ca3af; /* gray-400 */
  line-height: 1.8;
}

.footer-info p {
  margin: 0;
}

.copyright {
  margin-top: 20px;
  font-size: 11px;
  color: #d1d5db; /* gray-300 */
  font-weight: 600;
}

/* 반응형 모바일 최적화 */
@media (max-width: 768px) {
  .bg-circle {
    width: 90vw;
    height: 90vw;
  }
  .bg-donut {
    width: 150vw;
    height: 150vw;
  }

  .login-container {
    padding: 30px 20px;
  }

  .footer-info {
    display: none;
  }

  .copyright {
    margin-top: 40px;
  }
}
</style>
