<template>
  <div class="card flex flex-col items-center justify-center h-full">
    <!-- 아이콘 -->
    <div :class="[
      'w-14 h-14 rounded-full flex items-center justify-center mb-3',
      iconBgColor
    ]">
      <!-- Clock 아이콘 -->
      <svg v-if="icon === 'clock'" :class="['w-7 h-7', iconColor]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>

      <!-- Star 아이콘 -->
      <svg v-if="icon === 'star'" :class="['w-7 h-7', iconColor]" fill="currentColor" viewBox="0 0 24 24">
        <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
      </svg>
    </div>

    <!-- 값 (숫자) -->
    <p class="text-4xl font-bold text-gray-900 mb-2">{{ value }}</p>

    <!-- 레이블 -->
    <p class="text-sm text-gray-600">{{ title }}</p>
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

// 아이콘 배경색
const iconBgColor = computed(() => {
  return props.color === 'purple' ? 'bg-purple-100' : 'bg-yellow-100'
})

// 아이콘 색상
const iconColor = computed(() => {
  return props.color === 'purple' ? 'text-purple-600' : 'text-yellow-600'
})
</script>

<style scoped>
/* StatsCard 전용 스타일 */
</style>
