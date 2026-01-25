<template>
  <div class="dashboard-layout flex h-screen bg-gray-50">
    <!-- 좌측 사이드바 (고정 폭 256px) -->
    <AppSidebar class="w-64 flex-shrink-0" />

    <!-- 메인 컨텐츠 영역 -->
    <main class="flex-1 overflow-y-auto">
      <slot />
    </main>
  </div>
</template>

<script setup>
import AppSidebar from './AppSidebar.vue'
</script>

<style scoped>
/* DashboardLayout 전용 스타일 */
</style>
