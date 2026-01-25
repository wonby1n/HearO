<template>
  <div class="flex items-center gap-4">
    <!-- 통화 상태 인디케이터 -->
    <div class="flex items-center gap-2">
      <div class="relative">
        <div
          :class="[
            'w-3 h-3 rounded-full',
            isActive ? 'bg-green-500' : 'bg-gray-400'
          ]"
        ></div>
        <!-- 통화 중일 때 펄스 애니메이션 -->
        <div
          v-if="isActive"
          class="absolute inset-0 w-3 h-3 rounded-full bg-green-500 animate-ping opacity-75"
        ></div>
      </div>
      <span
        :class="[
          'text-sm font-medium',
          isActive ? 'text-green-600' : 'text-gray-600'
        ]"
      >
        {{ statusText }}
      </span>
    </div>

    <!-- 통화 시간 타이머 -->
    <div
      :class="[
        'text-2xl font-mono font-semibold',
        isActive ? 'text-gray-900' : 'text-gray-500'
      ]"
    >
      {{ formattedTime }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  isActive: {
    type: Boolean,
    default: false
  },
  startTime: {
    type: Number,
    default: null
  }
})

// 경과 시간 (초)
const elapsedSeconds = ref(0)

// 타이머 인터벌
let timerInterval = null

// 포맷된 시간 (MM:SS)
const formattedTime = computed(() => {
  const minutes = Math.floor(elapsedSeconds.value / 60)
  const seconds = elapsedSeconds.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

// 상태 텍스트
const statusText = computed(() => {
  return props.isActive ? '통화 중' : '대기 중'
})

// 타이머 시작
const startTimer = () => {
  if (timerInterval) {
    clearInterval(timerInterval)
  }

  timerInterval = setInterval(() => {
    elapsedSeconds.value++
  }, 1000)
}

// 타이머 중지
const stopTimer = () => {
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
}

// 타이머 리셋
const resetTimer = () => {
  stopTimer()
  elapsedSeconds.value = 0
}

// isActive가 변경되면 타이머 제어
const handleActiveChange = (newValue) => {
  if (newValue) {
    startTimer()
  } else {
    stopTimer()
  }
}

onMounted(() => {
  if (props.isActive) {
    startTimer()
  }
})

onUnmounted(() => {
  stopTimer()
})

// Watch isActive
import { watch } from 'vue'
watch(() => props.isActive, handleActiveChange)

// 외부에서 접근 가능하도록 expose
defineExpose({
  resetTimer,
  startTimer,
  stopTimer
})
</script>

<style scoped>
/* CallTimer 전용 스타일 */
</style>
