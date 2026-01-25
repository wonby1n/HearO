<template>
  <aside class="bg-primary-700 text-white flex flex-col">
    <!-- 로고 영역 -->
    <div class="p-6 flex items-center gap-3">
      <!-- HearO 로고 아이콘 (방패 모양) -->
      <div class="w-10 h-10 bg-white rounded-lg flex items-center justify-center">
        <svg class="w-6 h-6 text-primary-700" fill="currentColor" viewBox="0 0 24 24">
          <path d="M12 2L4 5v6.09c0 5.05 3.41 9.76 8 10.91 4.59-1.15 8-5.86 8-10.91V5l-8-3zm0 18.5c-3.76-1.08-6.5-5.06-6.5-9.41V6.3l6.5-2.6 6.5 2.6v4.79c0 4.35-2.74 8.33-6.5 9.41z"/>
        </svg>
      </div>
      <h1 class="text-2xl font-bold">HearO</h1>
    </div>

    <!-- 네비게이션 메뉴 -->
    <nav class="flex-1 px-3 py-4">
      <ul class="space-y-2">
        <!-- Dashboard 메뉴 -->
        <li>
          <router-link
            to="/"
            class="flex items-center px-6 py-3 text-white/80 hover:bg-primary-600 transition-colors cursor-pointer rounded-lg"
            active-class="bg-primary-800 text-white"
          >
            <svg class="w-6 h-6 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
            </svg>
            <span class="text-lg">Dashboard</span>
          </router-link>
        </li>

        <!-- Call History 메뉴 -->
        <li>
          <router-link
            to="/call-history"
            class="flex items-center px-6 py-3 text-white/80 hover:bg-primary-600 transition-colors cursor-pointer rounded-lg"
            active-class="bg-primary-800 text-white"
          >
            <svg class="w-6 h-6 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span class="text-lg">Call History</span>
          </router-link>
        </li>
      </ul>
    </nav>
  </aside>
</template>

<script setup>
// 네비게이션 로직 (현재는 router-link가 자동으로 처리)
</script>

<style scoped>
/* Tailwind v4에서는 템플릿에서 직접 클래스 사용 권장 */
</style>
