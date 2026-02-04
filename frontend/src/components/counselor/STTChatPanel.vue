<template>
  <div class="flex flex-col h-full bg-white rounded-lg shadow-sm border border-gray-200">
    <!-- 헤더 (flex-shrink-0) -->
    <div class="flex-shrink-0 px-6 py-4 border-b border-gray-200">
      <h3 class="text-lg font-semibold text-gray-900">실시간 자막</h3>
    </div>

    <!-- 폭언 감지 알림 (오른쪽 하단 토스트) -->
    <div class="stt-notification-container" role="alert" aria-live="polite" aria-atomic="true">
      <TransitionGroup name="notification-slide">
        <NotificationItem v-for="notification in notifications" :key="notification.id" :id="notification.id"
          :type="notification.type" :message="notification.message" :count="notification.count"
          @close="notificationStore.removeNotification(notification.id)" />
      </TransitionGroup>
    </div>

    <!-- 자막 영역 (flex-1 overflow-y-auto min-h-0) -->
    <div ref="chatContainer" class="flex-1 overflow-y-auto p-6 space-y-4 scrollbar-thin relative min-h-0"
      :class="callStore.currentCall.profanityCount >= 1 ? 'pt-20' : ''">
      <!-- 메시지 목록 -->
      <div v-for="(message, index) in messages" :key="index" class="flex flex-col" :class="message.speaker === 'agent' ? 'items-end' : 'items-start'">
        <!-- 이름 + 시간 (말풍선 위) -->
        <div class="flex items-center gap-2 mb-1 px-1">
          <span class="text-xs font-medium text-gray-600">{{ getSpeakerLabel(message.speaker) }}</span>
          <span class="text-xs text-gray-400">{{ message.timestamp }}</span>
        </div>

        <!-- 말풍선 -->
        <div :class="[
          'max-w-[70%] rounded-2xl px-4 py-3 shadow-sm',
          message.speaker === 'agent' ? 'bg-primary-600 text-white rounded-tr-sm' : 'bg-gray-100 text-gray-900 rounded-tl-sm',
          message.hasProfanity && !message.isProfanityCancelled ? 'ring-2 ring-red-400' : ''
        ]">
          <!-- 메시지 내용 -->
          <div class="text-sm leading-relaxed">
            <!-- 욕설이 있는 경우 블러 처리 -->
            <template v-if="message.hasProfanity && !message.isProfanityCancelled">
              <div class="flex items-center gap-2">
                <svg class="w-4 h-4 text-red-600 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd"
                    d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
                    clip-rule="evenodd" />
                </svg>
                <span v-if="!message.showOriginal" class="font-mono tracking-wider">
                  {{ message.maskedText }}
                </span>
                <span v-else>{{ message.text }}</span>
              </div>
            </template>

            <!-- 폭언 취소된 경우 일반 메시지로 표시 -->
            <template v-else-if="message.hasProfanity && message.isProfanityCancelled">
              <span class="opacity-70 line-through">{{ message.maskedText }}</span>
              <span class="ml-2">{{ message.text }}</span>
            </template>

            <!-- 일반 메시지 -->
            <template v-else>
              {{ message.text }}
            </template>
          </div>

          <!-- 신뢰도 표시 (낮은 경우) -->
          <div v-if="message.confidence && message.confidence < 0.7" class="mt-2 text-xs opacity-75"
            :class="message.speaker === 'agent' ? 'text-white' : 'text-gray-500'">
            신뢰도: {{ Math.round(message.confidence * 100) }}%
          </div>
        </div>

        <!-- 폭언 컨트롤 버튼 (말풍선 아래) -->
        <div v-if="message.hasProfanity && !message.isProfanityCancelled" class="flex items-center gap-2 mt-2 px-1">
          <button
            @click="$emit('toggle-profanity', index)"
            class="flex items-center gap-1 px-3 py-1.5 rounded-lg text-xs font-medium transition-all hover:scale-105"
            :class="message.showOriginal
              ? 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              : 'bg-primary-100 text-primary-700 hover:bg-primary-200'"
          >
            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path v-if="!message.showOriginal" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
              <path v-if="!message.showOriginal" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
              <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"/>
            </svg>
            {{ message.showOriginal ? '숨기기' : '확인' }}
          </button>
          <button
            @click="$emit('cancel-profanity', index)"
            class="flex items-center gap-1 px-3 py-1.5 bg-orange-100 text-orange-700 rounded-lg text-xs font-medium hover:bg-orange-200 transition-all hover:scale-105"
          >
            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
            오탐 취소
          </button>
        </div>
      </div>

      <!-- 빈 상태 -->
      <div v-if="messages.length === 0" class="flex items-center justify-center h-full text-gray-400">
        <div class="text-center">
          <svg class="w-16 h-16 mx-auto mb-3 opacity-50" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd"
              d="M18 10c0 3.866-3.582 7-8 7a8.841 8.841 0 01-4.083-.98L2 17l1.338-3.123C2.493 12.767 2 11.434 2 10c0-3.866 3.582-7 8-7s8 3.134 8 7zM7 9H5v2h2V9zm8 0h-2v2h2V9zM9 9h2v2H9V9z"
              clip-rule="evenodd" />
          </svg>
          <p class="text-sm">대화 내용이 여기에 표시됩니다</p>
        </div>
      </div>
    </div>

    <!-- 비속어 카운터 (상단 고정 배너) -->
    <Transition name="slide-down">
      <div v-if="callStore.currentCall.profanityCount >= 1" class="profanity-banner">
        <div class="flex items-center gap-3 px-4 py-3 bg-gradient-to-r from-red-50 to-orange-50 border-l-4 border-red-500 shadow-md">
          <svg class="w-5 h-5 text-red-600 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
          </svg>
          <div class="flex-1">
            <div :class="['font-bold text-sm', { 'counter-pulse': isCounterAnimating }]">
              <span class="text-red-700">비속어 {{ callStore.currentCall.profanityCount }}회 감지</span>
              <span class="text-gray-500 ml-2">(3회 시 자동 종료)</span>
            </div>
            <div v-if="callStore.currentCall.profanityCount >= 2" class="text-xs text-red-600 font-semibold mt-0.5">
              ⚠️ 1회 더 감지되면 통화가 종료됩니다
            </div>
            <div v-else class="text-xs text-orange-600 font-medium mt-0.5">
              비속어 사용을 자제해 주세요
            </div>
          </div>
          <div class="flex items-center justify-center w-10 h-10 rounded-full"
            :class="callStore.currentCall.profanityCount >= 2 ? 'bg-red-500' : 'bg-orange-500'">
            <span class="text-white font-bold text-lg">{{ callStore.currentCall.profanityCount }}</span>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 입력창 (flex-shrink-0) -->
    <div class="flex-shrink-0 px-4 py-3 border-t bg-gray-50">
      <form @submit.prevent="handleSubmit" class="flex gap-2">
        <input 
          v-model="counselorInput" 
          type="text" 
          placeholder="상담사 메시지 입력..." 
          :disabled="!isCallActive"
          class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent disabled:bg-gray-100 disabled:cursor-not-allowed"
        />
        <button 
          type="submit" 
          :disabled="!counselorInput.trim() || !isCallActive"
          class="px-6 py-2 bg-primary-600 text-white rounded-lg font-medium hover:bg-primary-700 transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed"
        >
          전송
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, computed, onMounted, onUnmounted } from 'vue'
import { useNotificationStore } from '@/stores/notification'
import { useCallStore } from '@/stores/call'
import NotificationItem from '@/components/notification/NotificationItem.vue'

// 스토어
const notificationStore = useNotificationStore()
const callStore = useCallStore()

// 개발용: 콘솔에서 테스트 가능하도록 window에 노출
if (import.meta.env.DEV) {
  // 콘솔에서 테스트 (production에서 자동 제거됨)
  window.__DEV_NOTIFICATION_STORE__ = notificationStore
}

// 알림 목록 (profanity 타입만 필터링)
const notifications = computed(() => {
  return notificationStore.notifications.filter(n => n.type === 'profanity')
})

// 카운터 애니메이션 상태
const isCounterAnimating = ref(false)

// 카운트 변경 시 애니메이션 트리거
watch(
  () => callStore.currentCall.profanityCount,
  (newCount, oldCount) => {
    if (newCount > oldCount) {
      isCounterAnimating.value = true
      setTimeout(() => {
        isCounterAnimating.value = false
      }, 600)
    }
  }
)

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  isCallActive: {
    type: Boolean,
    default: true
  },
  counselorName: {
    type: String,
    default: '상담원'
  }
})

const emit = defineEmits(['toggle-profanity', 'cancel-profanity', 'counselor-message'])

const chatContainer = ref(null)
const isUserScrolling = ref(false) // 사용자가 스크롤 중인지 여부
const counselorInput = ref('')

// 화자 라벨 반환
const getSpeakerLabel = (speaker) => {
  if (speaker === 'agent') return props.counselorName
  return speaker === 'customer' ? '고객' : speaker
}

// 맨 아래에 있는지 확인 (여유 50px)
const isAtBottom = () => {
  if (!chatContainer.value) return true
  const { scrollTop, scrollHeight, clientHeight } = chatContainer.value
  return scrollHeight - scrollTop - clientHeight < 50
}

// 스크롤 이벤트 핸들러
const handleScroll = () => {
  if (!chatContainer.value) return

  // 사용자가 맨 아래로 스크롤하면 자동 스크롤 재개
  if (isAtBottom()) {
    isUserScrolling.value = false
  } else {
    // 사용자가 위로 스크롤하면 자동 스크롤 중지
    isUserScrolling.value = true
  }
}

// 새 메시지가 추가되면 자동 스크롤 (사용자가 스크롤 중이 아닐 때만)
watch(
  () => props.messages.length,
  async () => {
    await nextTick()

    // 사용자가 스크롤 중이 아니거나, 맨 아래에 있을 때만 자동 스크롤
    if (chatContainer.value && !isUserScrolling.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  }
)

// 상담사 메시지 전송 핸들러
const handleSubmit = () => {
  const message = counselorInput.value.trim()
  if (!message || !props.isCallActive) return
  
  emit('counselor-message', message)
  counselorInput.value = ''
}

// 컴포넌트 마운트 시 스크롤 이벤트 리스너 등록
onMounted(() => {
  if (chatContainer.value) {
    chatContainer.value.addEventListener('scroll', handleScroll)
  }
})

// 컴포넌트 언마운트 시 이벤트 리스너 제거
onUnmounted(() => {
  if (chatContainer.value) {
    chatContainer.value.removeEventListener('scroll', handleScroll)
  }
})
</script>

<style scoped>
/* STTChatPanel 전용 스타일 */
.scrollbar-thin::-webkit-scrollbar {
  width: 6px;
}

.scrollbar-thin::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.scrollbar-thin::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 3px;
}

.scrollbar-thin::-webkit-scrollbar-thumb:hover {
  background: #555;
}

/* 알림 컨테이너 - 오른쪽 하단 토스트 */
.stt-notification-container {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 50;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
  pointer-events: none;
  max-width: 400px;
}

.stt-notification-container>* {
  pointer-events: auto;
}

/* TransitionGroup 애니메이션 - 슬라이드 */
.notification-slide-enter-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.notification-slide-leave-active {
  transition: all 0.3s ease-out;
}

.notification-slide-enter-from {
  opacity: 0;
  transform: translateX(100px) scale(0.8);
}

.notification-slide-leave-to {
  opacity: 0;
  transform: translateX(50px) scale(0.9);
}

.notification-slide-move {
  transition: transform 0.3s ease;
}

/* 비속어 배너 - 상단 고정 */
.profanity-banner {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 30;
}

/* 슬라이드 다운 애니메이션 */
.slide-down-enter-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.slide-down-leave-active {
  transition: all 0.3s ease-out;
}

.slide-down-enter-from {
  opacity: 0;
  transform: translateY(-100%);
}

.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-100%);
}

/* 카운터 펄스 애니메이션 */
.counter-pulse {
  display: inline-block;
  animation: counterPulse 0.6s ease-out;
}

@keyframes counterPulse {
  0% {
    transform: scale(1);
  }

  25% {
    transform: scale(1.3);
    color: #dc2626;
    text-shadow: 0 0 10px rgba(220, 38, 38, 0.5);
  }

  50% {
    transform: scale(1.1);
  }

  100% {
    transform: scale(1);
  }
}
</style>
