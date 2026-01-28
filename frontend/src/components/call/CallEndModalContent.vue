<template>
  <div>
    <div class="icon-container">
      <svg class="ai-shield" viewBox="0 0 24 24" fill="none" stroke="currentColor" aria-hidden="true">
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="1.6"
          d="M12 3l7 3v6c0 4.5-3.1 8.2-7 9-3.9-.8-7-4.5-7-9V6l7-3z"
        />
        <path
          d="M12 8.5l.9 1.9 2.1.3-1.5 1.4.4 2.1-1.9-1-1.9 1 .4-2.1-1.5-1.4 2.1-.3.9-1.9z"
          fill="currentColor"
          stroke="none"
        />
      </svg>
    </div>

    <h2 class="modal-title">{{ title }}</h2>

    <div class="message-container">
      <p v-if="message" class="main-message">{{ message }}</p>

      <slot name="extra"></slot>

      <div class="summary-section">
        <h3 class="section-title">AI 요약</h3>
        <div class="summary-card">
          <p class="summary-text">{{ summaryText }}</p>
        </div>
      </div>

      <div class="memo-section">
        <div class="section-header">
          <h3 class="section-title">상담 메모 이어쓰기</h3>
          <span class="memo-hint">자동 저장</span>
        </div>
        <textarea
          v-model="memoValue"
          class="memo-textarea"
          placeholder="이어서 메모를 작성하세요."
          rows="4"
        ></textarea>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  message: {
    type: String,
    default: ''
  },
  aiSummary: {
    type: String,
    default: ''
  },
  memo: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:memo'])

const summaryText = computed(() => {
  const normalized = props.aiSummary?.trim()
  return normalized && normalized.length > 0
    ? normalized
    : 'AI 요약이 아직 준비되지 않았습니다.'
})

const memoValue = computed({
  get: () => props.memo,
  set: (value) => emit('update:memo', value)
})
</script>

<style scoped>
.icon-container {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #e0f2fe 0%, #dbeafe 100%);
  border: 1px solid #bae6fd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-container svg {
  width: 34px;
  height: 34px;
  color: #0284c7;
}

.modal-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  text-align: center;
  margin: 0 0 24px 0;
}

.message-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 24px;
}

.main-message {
  font-size: 15px;
  color: #4b5563;
  text-align: center;
  line-height: 1.6;
  margin: 0;
  white-space: pre-line;
}

.summary-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 14px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 8px 0;
}

.summary-card {
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 12px;
  padding: 12px 14px;
}

.summary-text {
  font-size: 13px;
  color: #0f172a;
  line-height: 1.6;
  margin: 0;
  white-space: pre-line;
}

.memo-section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.memo-hint {
  font-size: 12px;
  color: #6b7280;
}

.memo-textarea {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 13px;
  line-height: 1.5;
  resize: vertical;
  min-height: 96px;
  background: #ffffff;
}

.memo-textarea:focus {
  outline: none;
  border-color: #60a5fa;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
}
</style>
