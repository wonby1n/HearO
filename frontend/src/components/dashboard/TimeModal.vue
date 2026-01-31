<template>
  <Transition
    enter-active-class="transition duration-500 ease-out"
    enter-from-class="opacity-0 scale-95"
    enter-to-class="opacity-100 scale-100"
    leave-active-class="transition duration-300 ease-in"
    leave-from-class="opacity-100 scale-100"
    leave-to-class="opacity-0 scale-95"
  >
    <div
      v-if="modelValue"
      class="fixed inset-0 flex items-center justify-center bg-slate-900/50 backdrop-blur-[8px] z-50 p-6"
      @click.self="handleClose"
    >
      <!-- HearO Protector Modal Card -->
      <div class="relative w-full max-w-[520px] bg-white rounded-[56px] shadow-[0_40px_100px_-20px_rgba(0,0,0,0.18)] overflow-hidden">
        <!-- 테스트용 닫기 버튼 -->
        <button
          @click="forceClose"
          class="absolute top-6 right-6 z-20 w-10 h-10 flex items-center justify-center rounded-full bg-slate-100 hover:bg-slate-200 transition-colors group"
          title="테스트용 강제 닫기"
        >
          <svg class="w-5 h-5 text-slate-600 group-hover:text-slate-800" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>

        <div class="relative p-12 flex flex-col items-center">
          <!-- Header: System Status -->
          <div class="text-center z-10 mb-10">
            <div class="inline-flex items-center gap-2.5 px-5 py-2 bg-emerald-50 text-emerald-600 rounded-full mb-6 border border-emerald-100 shadow-sm">
              <span class="relative flex h-2 w-2">
                <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                <span class="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
              </span>
              <span class="text-[11px] font-bold tracking-[0.15em] uppercase">Counselor Protection Active</span>
            </div>
            <h2 class="text-slate-400 text-lg font-medium mb-1">상담원님을 보호하기 위한</h2>
            <h1 class="text-[#1F3A8C] text-3xl font-black tracking-tight leading-tight">의무 휴식 중입니다</h1>
          </div>

          <!-- Central Protection Unit -->
          <div class="relative w-80 h-80 flex items-center justify-center mb-12">
            <!-- Outer Orbital Rings -->
            <div class="absolute inset-0 border border-slate-100 rounded-full"></div>
            <div class="absolute inset-4 border border-dashed border-slate-200/60 rounded-full animate-rotate-slow"></div>

            <!-- Progress Ring -->
            <svg class="absolute inset-0 w-full h-full -rotate-90 scale-[1.1]">
              <circle cx="160" cy="160" r="115" fill="transparent" stroke="#f1f5f9" stroke-width="8" />
              <circle
                cx="160"
                cy="160"
                r="115"
                fill="transparent"
                stroke="url(#protectionGradient)"
                stroke-width="12"
                :stroke-dasharray="strokeDasharray"
                :stroke-dashoffset="strokeDashoffset"
                stroke-linecap="round"
                style="transition: stroke-dashoffset 1s linear; filter: drop-shadow(0 0 15px rgba(31, 58, 140, 0.2));"
              />
              <defs>
                <linearGradient id="protectionGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                  <stop offset="0%" stop-color="#1F3A8C" />
                  <stop offset="100%" stop-color="#4F46E5" />
                </linearGradient>
              </defs>
            </svg>

            <!-- White Core Center -->
            <div class="relative w-60 h-60 bg-white rounded-full flex flex-col items-center justify-center z-10 shadow-[inset_0_2px_20px_rgba(0,0,0,0.03),0_25px_60px_rgba(31,58,140,0.12)] border border-slate-50 animate-breathe">
              <div class="mb-4">
                <div class="p-4 bg-blue-50 rounded-full text-[#1F3A8C]">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                    <path d="M12 8v4l3 3" opacity="0.5"/>
                  </svg>
                </div>
              </div>

              <div class="text-[56px] font-black text-[#1F3A8C] tracking-tighter tabular-nums leading-none">
                {{ formattedTime }}
              </div>

              <div class="mt-4 px-4 py-1.5 bg-[#f0f3ff] rounded-full border border-blue-100">
                <span class="text-[#1F3A8C] text-[10px] font-bold tracking-[0.2em] uppercase">마음 회복 시간</span>
              </div>
            </div>
          </div>

          <!-- Protective Message -->
          <div class="text-center z-10 mb-12">
            <p class="text-slate-500 text-xl font-medium leading-relaxed">
              상담원님의 <span class="text-[#1F3A8C] font-bold">소중한 마음</span>을<br />
              먼저 챙겨주세요. 🌸
            </p>
            <p class="mt-4 text-slate-400 text-sm">
              10분의 의무 휴식 후 업무 복귀가 가능합니다.
            </p>
          </div>

          <!-- Confirm/Return Button -->
          <button
            @click="handleClose"
            class="btn-primary w-full py-6 rounded-[32px] text-xl font-bold shadow-xl active:scale-[0.98]"
            :disabled="timeLeft > 0"
            :class="{ 'opacity-50 cursor-not-allowed': timeLeft > 0 }"
          >
            {{ timeLeft > 0 ? '휴식 중...' : '휴식 종료 및 복귀' }}
          </button>
        </div>

        <!-- Subtle Bottom Progress Indicator -->
        <div class="w-full h-3 bg-slate-50 flex">
          <div
            class="h-full bg-gradient-to-r from-[#1F3A8C] via-[#4F46E5] to-[#1F3A8C] transition-all duration-1000"
            :style="{ width: (timeLeft/totalTime)*100 + '%' }"
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
    default: 600 // 10분 (600초)
  }
})

const emit = defineEmits(['update:modelValue', 'complete'])

const totalTime = props.duration
const timeLeft = ref(props.duration)
let timerInterval = null

const radius = 115
const strokeDasharray = 2 * Math.PI * radius

const formattedTime = computed(() => {
  const mins = Math.floor(timeLeft.value / 60)
  const secs = timeLeft.value % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
})

const strokeDashoffset = computed(() => {
  const progress = timeLeft.value / totalTime
  return strokeDasharray * (1 - progress)
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
/* HearO Project Custom Theme Variables */
:root {
  --primary-50: #f0f3ff;
  --primary-600: #1F3A8C; /* HearO Navy */
  --primary-700: #1a3278;
  --safety-green: #10b981;
}

/* Custom Component Styles */
.btn-primary {
  background-color: var(--primary-600);
  color: white;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.btn-primary:hover:not(:disabled) {
  background-color: var(--primary-700);
  transform: translateY(-2px);
  box-shadow: 0 12px 30px -5px rgba(31, 58, 140, 0.35);
}

/* Animations for "Protection" Feel */
@keyframes breathe {
  0%, 100% { transform: scale(1); box-shadow: 0 0 20px rgba(31, 58, 140, 0.1); }
  50% { transform: scale(1.03); box-shadow: 0 0 40px rgba(31, 58, 140, 0.2); }
}

@keyframes rotate-slow {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.animate-breathe {
  animation: breathe 4s ease-in-out infinite;
}

.animate-rotate-slow {
  animation: rotate-slow 40s linear infinite;
}

.glass-panel {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
}
</style>
