<template>
  <div class="flex flex-col h-full bg-white rounded-lg shadow-sm border border-gray-200">
    <!-- 헤더 -->
    <div class="px-6 py-4 border-b border-gray-200">
      <h3 class="text-lg font-semibold text-gray-900">실시간 자막</h3>
    </div>

    <!-- 자막 영역 (채팅 형태) -->
    <div
      ref="chatContainer"
      class="flex-1 overflow-y-auto p-6 space-y-4 scrollbar-thin"
    >
      <!-- 폭언 감지 경고 (상단 고정) -->
      <div
        v-if="profanityCount > 0"
        class="sticky top-0 bg-red-50 border border-red-300 rounded-lg p-4 flex items-center gap-3 shadow-sm z-10"
      >
        <svg class="w-6 h-6 text-red-600 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
        </svg>
        <div class="flex-1">
          <p class="text-sm font-semibold text-red-900">폭언 감지 : {{ profanityCount }}회</p>
        </div>
      </div>

      <!-- 메시지 목록 -->
      <div
        v-for="(message, index) in messages"
        :key="index"
        :class="[
          'flex',
          message.speaker === 'agent' ? 'justify-end' : 'justify-start'
        ]"
      >
        <div
          :class="[
            'max-w-[70%] rounded-lg px-4 py-3 shadow-sm',
            message.speaker === 'agent'
              ? 'bg-primary-600 text-white'
              : 'bg-gray-100 text-gray-900'
          ]"
        >
          <!-- 화자 라벨 + 시간 -->
          <div class="flex items-center justify-between gap-2 mb-1">
            <span
              :class="[
                'text-xs font-medium',
                message.speaker === 'agent' ? 'text-white opacity-90' : 'text-gray-600'
              ]"
            >
              {{ getSpeakerLabel(message.speaker) }}
            </span>
            <span
              :class="[
                'text-xs',
                message.speaker === 'agent' ? 'text-white opacity-75' : 'text-gray-500'
              ]"
            >
              {{ message.timestamp }}
            </span>
          </div>

          <!-- 메시지 내용 -->
          <div class="text-sm leading-relaxed">
            <!-- 욕설이 있는 경우 블러 처리 -->
            <template v-if="message.hasProfanity">
              <div class="flex items-center gap-2">
                <svg class="w-4 h-4 text-red-600 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
                </svg>
                <span
                  v-if="!message.showOriginal"
                  class="font-mono tracking-wider"
                >
                  {{ message.maskedText }}
                </span>
                <span v-else>{{ message.text }}</span>
                <button
                  @click="$emit('toggle-profanity', index)"
                  class="text-xs underline hover:no-underline"
                  :class="message.speaker === 'agent' ? 'text-white' : 'text-primary-600'"
                >
                  {{ message.showOriginal ? '숨기기' : '확인' }}
                </button>
              </div>
            </template>

            <!-- 일반 메시지 -->
            <template v-else>
              {{ message.text }}
            </template>
          </div>

          <!-- 신뢰도 표시 (낮은 경우) -->
          <div
            v-if="message.confidence && message.confidence < 0.7"
            class="mt-2 text-xs opacity-75"
            :class="message.speaker === 'agent' ? 'text-white' : 'text-gray-500'"
          >
            신뢰도: {{ Math.round(message.confidence * 100) }}%
          </div>
        </div>
      </div>

      <!-- 폭언 경고 배너 (3회 감지 경고) -->
      <div
        v-if="profanityCount >= 1"
        class="bg-red-100 border-2 border-red-500 rounded-lg p-4 text-center"
      >
        <p class="text-red-900 font-bold text-sm">비속어 총 {{ profanityCount }}회 감지</p>
        <p class="text-red-700 text-xs mt-1">3회가 감지되면 통화종료됩니다</p>
      </div>

      <!-- 빈 상태 -->
      <div
        v-if="messages.length === 0"
        class="flex items-center justify-center h-full text-gray-400"
      >
        <div class="text-center">
          <svg class="w-16 h-16 mx-auto mb-3 opacity-50" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M18 10c0 3.866-3.582 7-8 7a8.841 8.841 0 01-4.083-.98L2 17l1.338-3.123C2.493 12.767 2 11.434 2 10c0-3.866 3.582-7 8-7s8 3.134 8 7zM7 9H5v2h2V9zm8 0h-2v2h2V9zM9 9h2v2H9V9z" clip-rule="evenodd"/>
          </svg>
          <p class="text-sm">대화 내용이 여기에 표시됩니다</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  profanityCount: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['toggle-profanity'])

const chatContainer = ref(null)

// 화자 라벨 반환
const getSpeakerLabel = (speaker) => {
  const labels = {
    agent: '상담원',
    customer: '고객',
    ai: 'AI 제안'
  }
  return labels[speaker] || speaker
}

// 새 메시지가 추가되면 자동 스크롤
watch(
  () => props.messages.length,
  async () => {
    await nextTick()
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  }
)
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
</style>
