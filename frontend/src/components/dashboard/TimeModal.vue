<template>
  <Transition
    enter-active-class="transition duration-300 ease-out"
    enter-from-class="opacity-0 scale-95"
    enter-to-class="opacity-100 scale-100"
    leave-active-class="transition duration-200 ease-in"
    leave-from-class="opacity-100 scale-100"
    leave-to-class="opacity-0 scale-95"
  >
    <div
      v-if="modelValue"
      class="fixed inset-0 flex items-center justify-center bg-black/40 backdrop-blur-sm z-50 p-6"
      @click.self="handleClose"
    >
      <!-- Compact Professional Modal -->
      <div class="relative w-full max-w-md bg-white rounded-2xl shadow-2xl overflow-hidden">
        <!-- 테스트용 닫기 버튼 -->
        <button
          @click="forceClose"
          class="absolute top-3 right-3 z-20 w-8 h-8 flex items-center justify-center rounded-full bg-gray-100 hover:bg-gray-200 transition-colors"
          title="테스트용 강제 닫기"
        >
          <svg class="w-4 h-4 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>

        <div class="p-8">
          <!-- Header -->
          <div class="text-center mb-6">
            <div class="inline-flex items-center gap-2 px-3 py-1.5 bg-blue-50 text-primary-600 rounded-full mb-3 border border-primary-100">
              <span class="relative flex h-2 w-2">
                <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-primary-400 opacity-75"></span>
                <span class="relative inline-flex rounded-full h-2 w-2 bg-primary-500"></span>
              </span>
              <span class="text-xs font-semibold">의무 휴식 중</span>
            </div>
            <h1 class="text-2xl font-bold text-gray-900 mb-2">보호 시스템 가동</h1>
            <p class="text-sm text-gray-500">잠시 휴식을 취해주세요</p>
          </div>

          <!-- Timer Circle -->
          <div class="relative w-56 h-56 mx-auto mb-6">
            <!-- Progress Ring -->
            <svg class="absolute inset-0 w-full h-full -rotate-90">
              <circle cx="112" cy="112" r="100" fill="transparent" stroke="#E5E7EB" stroke-width="8" />
              <circle
                cx="112"
                cy="112"
                r="100"
                fill="transparent"
                stroke="#1F3A8C"
                stroke-width="8"
                :stroke-dasharray="strokeDasharray"
                :stroke-dashoffset="strokeDashoffset"
                stroke-linecap="round"
                class="transition-all duration-1000"
              />
            </svg>

            <!-- Timer Display -->
            <div class="absolute inset-0 flex flex-col items-center justify-center">
              <div class="mb-2">
                <div class="p-3 bg-primary-50 rounded-full">
                  <svg class="w-6 h-6 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
              </div>
              <div class="text-5xl font-bold text-gray-900 tabular-nums">
                {{ formattedTime }}
              </div>
              <div class="mt-2 text-sm font-medium text-gray-500">
                남은 시간
              </div>
            </div>
          </div>

          <!-- Message -->
          <div class="text-center mb-6">
            <p class="text-gray-700 leading-relaxed">
              상담원님의 <span class="font-semibold text-primary-600">심리적 안정</span>을 위한<br />
              10분간의 의무 휴식입니다.
            </p>
          </div>

          <!-- Button -->
          <button
            @click="handleClose"
            class="w-full py-3.5 rounded-xl font-semibold transition-all shadow-sm"
            :disabled="timeLeft > 0"
            :class="timeLeft > 0 
              ? 'bg-gray-100 text-gray-400 cursor-not-allowed' 
              : 'bg-primary-600 text-white hover:bg-primary-700 active:scale-[0.98] shadow-md'"
          >
            {{ timeLeft > 0 ? '휴식 중...' : '업무 복귀' }}
          </button>
        </div>

        <!-- Progress Bar -->
        <div class="w-full h-1.5 bg-gray-100">
          <div
            class="h-full bg-primary-600 transition-all duration-1000"
            :style="{ width: ((totalTime - timeLeft) / totalTime * 100) + '%' }"
          ></div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  duration: {
    type: Number,
    default: 10 // 10분 (600초)
  }
})

const emit = defineEmits(['update:modelValue', 'complete'])

const totalTime = props.duration
const timeLeft = ref(props.duration)
let timerInterval = null

const radius = 100
const strokeDasharray = 2 * Math.PI * radius

const formattedTime = computed(() => {
  const mins = Math.floor(timeLeft.value / 60)
  const secs = timeLeft.value % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
})

const strokeDashoffset = computed(() => {
  const progress = timeLeft.value / totalTime
  return strokeDasharray * progress
})

const handleClose = () => {
  if (timeLeft.value > 0) {
    // 시간이 남아있으면 닫기 불가
    return
  }
  emit('update:modelValue', false)
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
}

// 테스트용 강제 닫기
const forceClose = () => {
  console.log('[TimeModal] 테스트용 강제 닫기')
  emit('update:modelValue', false)
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
}

const startTimer = () => {
  if (timerInterval) clearInterval(timerInterval)
  timerInterval = setInterval(() => {
    if (timeLeft.value > 0) {
      timeLeft.value--
    } else {
      // 0초가 되면 자동으로 종료
      clearInterval(timerInterval)
      timerInterval = null
      emit('complete')
    }
  }, 1000)
}

// 모달이 열릴 때 타이머 시작
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    timeLeft.value = totalTime
    startTimer()
  } else {
    if (timerInterval) {
      clearInterval(timerInterval)
      timerInterval = null
    }
  }
})

onMounted(() => {
  if (props.modelValue) {
    startTimer()
  }
})

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
})
</script>

<style scoped>
/* Primary color variable */
:root {
  --primary-50: #f0f3ff;
  --primary-100: #e0e7ff;
  --primary-400: #818cf8;
  --primary-500: #6366f1;
  --primary-600: #1F3A8C;
  --primary-700: #1a3278;
}

.text-primary-600 {
  color: var(--primary-600);
}

.bg-primary-50 {
  background-color: var(--primary-50);
}

.bg-primary-600 {
  background-color: var(--primary-600);
}

.bg-primary-700 {
  background-color: var(--primary-700);
}

.border-primary-100 {
  border-color: var(--primary-100);
}
</style>
