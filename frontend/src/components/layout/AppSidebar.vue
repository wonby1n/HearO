<template>
  <aside class="sidebar-wrapper h-screen flex flex-col border-r border-white/10 transition-all duration-300">
    <!-- 로고 영역: 파란색 테마와 조화로운 로고 -->
    <div class="logo-section p-8 mb-4 flex items-center gap-4 group cursor-pointer">
      <div class="logo-box w-10 h-10 bg-white rounded-xl flex items-center justify-center shadow-lg shadow-black/10 group-hover:scale-110 transition-transform duration-300">
        <svg class="w-6 h-6 text-primary-main" fill="currentColor" viewBox="0 0 24 24">
          <path d="M12 2L4 5v6.09c0 5.05 3.41 9.76 8 10.91 4.59-1.15 8-5.86 8-10.91V5l-8-3zm0 18.5c-3.76-1.08-6.5-5.06-6.5-9.41V6.3l6.5-2.6 6.5 2.6v4.79c0 4.35-2.74 8.33-6.5 9.41z"/>
        </svg>
      </div>
      <h1 class="logo-text text-2xl font-black text-white tracking-tighter">Hear<span class="text-white/80">O</span></h1>
    </div>

    <!-- 네비게이션 메뉴: 파란색 배경과 대비되는 화이트/블루 조합 -->
    <nav class="flex-1 px-4 space-y-1.5">
      <div class="menu-group-label px-4 text-[10px] font-bold text-white/40 uppercase tracking-[0.2em] mb-4">
        Main Menu
      </div>
      
      <ul class="space-y-2">
        <!-- Dashboard 메뉴 -->
        <li>
          <router-link
            to="/"
            class="nav-item flex items-center px-4 py-3 text-white/70 hover:text-white transition-all duration-300 rounded-xl group relative overflow-hidden"
            active-class="active-nav"
          >
            <div class="icon-wrapper mr-3 transition-transform group-hover:scale-110">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
              </svg>
            </div>
            <span class="text-[15px] font-semibold tracking-wide">Dashboard</span>
            <!-- Active Indicator -->
            <div class="active-bar absolute left-0 w-1 h-6 bg-white rounded-r-full transform -translate-x-full transition-transform duration-300"></div>
          </router-link>
        </li>

        <!-- Call History 메뉴 -->
        <li>
          <router-link
            to="/call-history"
            class="nav-item flex items-center px-4 py-3 text-white/70 hover:text-white transition-all duration-300 rounded-xl group relative overflow-hidden"
            active-class="active-nav"
          >
            <div class="icon-wrapper mr-3 transition-transform group-hover:scale-110">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <span class="text-[15px] font-semibold tracking-wide">Call History</span>
            <div class="active-bar absolute left-0 w-1 h-6 bg-white rounded-r-full transform -translate-x-full transition-transform duration-300"></div>
          </router-link>
        </li>
      </ul>
    </nav>

    <!-- 푸터 영역: 로그아웃 -->
    <div class="p-6 border-t border-white/10 bg-black/10">
      <button
        @click="handleLogout"
        class="logout-btn w-full flex items-center justify-center gap-2 px-4 py-3 text-white/60 hover:text-white hover:bg-white/10 rounded-xl transition-all duration-300 group"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
        </svg>
        <span class="text-sm font-bold">Sign Out</span>
      </button>
    </div>
  </aside>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 로그아웃 핸들러
const handleLogout = async () => {
  console.log('[Sidebar] 로그아웃 버튼 클릭')

  // 로그아웃 실행 (auth store에서 자동으로 로그인 페이지로 리다이렉트)
  await authStore.logout()
}
</script>

<style scoped>
/**
 * 테마 변수 및 커스텀 스타일
 */
.sidebar-wrapper {
  --primary-main: #1F3A8C; /* 심신 안정에 도움을 주는 메인 블루 */
  --primary-dark: #1a3278;
  
  background-color: var(--primary-main);
  width: 280px;
}

.text-primary-main {
  color: var(--primary-main);
}

/* 활성 메뉴 아이템 스타일 */
.nav-item.active-nav {
  background-color: rgba(255, 255, 255, 0.15); /* 화이트 오버레이로 활성화 표시 */
  color: #ffffff;
}

.nav-item.active-nav .icon-wrapper {
  color: #ffffff;
}

.nav-item.active-nav .active-bar {
  transform: translateX(0);
}

/* 호버 시 미세 강조 */
.nav-item:hover:not(.active-nav) {
  background-color: rgba(255, 255, 255, 0.05);
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
}
</style>