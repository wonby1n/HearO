<template>
  <!-- [피드백 반영] data-color 속성을 통해 CSS 변수를 제어합니다. -->
  <div 
    class="stats-card card flex flex-col items-center justify-center h-full p-4 bg-white rounded-2xl shadow-sm border border-gray-50 transition-all hover:shadow-md"
    :data-color="color"
  >
    <!-- 아이콘 영역: 크기를 14에서 10으로 축소 -->
    <div class="icon-bg w-10 h-10 rounded-full flex items-center justify-center mb-2 transition-colors duration-300">
      <!-- Clock 아이콘 -->
      <svg v-if="icon === 'clock'" class="status-icon w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>

      <!-- Star 아이콘 -->
      <svg v-if="icon === 'star'" class="status-icon w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
        <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
      </svg>
    </div>

    <!-- 값 (숫자): 4xl에서 xl로 대폭 축소하여 레이아웃 최적화 -->
    <p class="stats-value text-xl font-bold text-gray-900 mb-0.5 tracking-tight">
      {{ value }}
    </p>

    <!-- 레이블: 폰트 사이즈 조정 -->
    <p class="text-[10px] font-semibold text-gray-500 uppercase tracking-wider">
      {{ title }}
    </p>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  icon: {
    type: String,
    required: true,
    validator: (value) => ['clock', 'star'].includes(value)
  },
  title: {
    type: String,
    required: true
  },
  value: {
    type: [String, Number],
    required: true
  },
  color: {
    type: String,
    required: true,
    validator: (value) => ['purple', 'yellow'].includes(value)
  }
})
</script>

<style scoped>
/**
 * [피드백 반영] 하드코딩된 색상을 CSS 변수로 관리하여 유지보수성을 높입니다.
 */
.stats-card {
  --stats-theme-color: #6366f1; /* Default Purple */
  --stats-bg-color: #f5f3ff;
}

.stats-card[data-color="purple"] {
  --stats-theme-color: #8b5cf6;
  --stats-bg-color: #f5f3ff;
}

.stats-card[data-color="yellow"] {
  --stats-theme-color: #f59e0b;
  --stats-bg-color: #fffbeb;
}

/* 아이콘 배경 스타일 */
.icon-bg {
  background-color: var(--stats-bg-color);
}

/* 아이콘 색상 스타일 */
.status-icon {
  color: var(--stats-theme-color);
}

/* 값(숫자) 강조 효과 (선택 사항) */
.stats-value {
  /* 숫자가 너무 커서 삐져나오는 것을 방지 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 호버 시 테마 색상으로 살짝 강조 */
.stats-card:hover {
  border-color: var(--stats-theme-color);
}
</style>