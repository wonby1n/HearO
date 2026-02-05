<template>
  <div>
    <!-- 알림 아이콘 -->
    <div class="icon-container">
      <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
      </svg>
    </div>

    <h2 class="modal-title">{{ title }}</h2>

    <div class="message-container">
      <p v-if="message" class="main-message">{{ message }}</p>

      <slot name="extra"></slot>

      <!-- AI 요약 섹션 -->
      <div class="ai-summary-section">
        <div class="ai-header">
          <div class="ai-badge">
            <span>{{ aiSummaryData ? 'AI 분석 완료' : 'AI 요약 생성 중...' }}</span>
          </div>
        </div>

        <!-- 로딩 상태 -->
        <div v-if="!aiSummaryData" class="loading-container">
          <div class="spinner"></div>
          <p class="loading-text">AI가 상담 내용을 분석하고 있습니다...</p>
        </div>

        <!-- AI 요약 완료 -->
        <div v-else>
          <!-- Title & Subtitle -->
          <div v-if="aiSummaryData?.title || aiSummaryData?.subtitle" class="ai-title-section">
            <h3 v-if="aiSummaryData?.title" class="ai-title">{{ aiSummaryData.title }}</h3>
            <p v-if="aiSummaryData?.subtitle" class="ai-subtitle">{{ aiSummaryData.subtitle }}</p>
          </div>

          <!-- AI Summary -->
          <div class="ai-summary-card">
            <!-- 섹션별로 파싱된 경우 -->
            <div v-if="parsedSummary" class="summary-sections">
              <div v-for="(section, index) in parsedSummary" :key="index" class="summary-section">
                <div class="section-header-row">
                  <svg class="section-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <path v-if="section.title === '이슈 요약'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                    <path v-else-if="section.title === '고객 요청'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"/>
                    <path v-else-if="section.title === '상담원 안내'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                    <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"/>
                  </svg>
                  <span class="section-title-text">{{ section.title }}</span>
                </div>
                <p class="section-content">{{ section.content }}</p>
              </div>
            </div>
            
            <!-- 파싱되지 않은 경우 전체 텍스트 표시 -->
            <div v-else>
              <div class="ai-summary-header">
                <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
                </svg>
                <span>상담 요약</span>
              </div>
              <p class="ai-summary-text">{{ summaryText }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 메모 섹션 -->
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
    type: [String, Object],
    default: ''
  },
  memo: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:memo'])

// AI 요약 데이터 처리 (객체 또는 문자열 지원)
const aiSummaryData = computed(() => {
  if (typeof props.aiSummary === 'object' && props.aiSummary !== null) {
    return props.aiSummary
  }
  // 하위 호환성: 문자열인 경우
  return {
    title: '',
    subtitle: '',
    aiSummary: props.aiSummary
  }
})

const summaryText = computed(() => {
  const summary = aiSummaryData.value?.aiSummary?.trim()
  return summary && summary.length > 0
    ? summary
    : 'AI 요약이 아직 준비되지 않았습니다.'
})

// AI 요약을 섹션별로 파싱
const parsedSummary = computed(() => {
  const summary = aiSummaryData.value?.aiSummary?.trim()
  if (!summary || summary.length === 0) {
    return null
  }

  const sections = []
  const sectionTitles = ['이슈 요약', '고객 요청', '상담원 안내', '후속 조치']
  
  sectionTitles.forEach((title, index) => {
    const nextTitle = sectionTitles[index + 1]
    const regex = nextTitle 
      ? new RegExp(`${title}[:\\s]*([\\s\\S]*?)(?=${nextTitle})`, 'i')
      : new RegExp(`${title}[:\\s]*([\\s\\S]*)`, 'i')
    
    const match = summary.match(regex)
    if (match && match[1]) {
      sections.push({
        title,
        content: match[1].trim()
      })
    }
  })

  return sections.length > 0 ? sections : null
})

const memoValue = computed({
  get: () => props.memo,
  set: (value) => emit('update:memo', value)
})
</script>

<style scoped>
/* 알림 아이콘 */
.icon-container {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #f0f3ff 0%, #e0e7ff 100%);
  border: 2px solid #1F3A8C;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon {
  width: 32px;
  height: 32px;
  color: #1F3A8C;
}

.modal-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  text-align: center;
  margin: 0 0 28px 0;
}

.message-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
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

/* AI 요약 섹션 - main.css primary 색상 사용 */
.ai-summary-section {
  background: linear-gradient(135deg, #f0f3ff 0%, #e0e7ff 100%);
  border: 2px solid #1F3A8C;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(31, 58, 140, 0.15);
}

.ai-header {
  margin-bottom: 20px;
}

.ai-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: #1F3A8C;
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(31, 58, 140, 0.3);
}

.ai-title-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #c7d2fe;
}

.ai-title {
  font-size: 20px;
  font-weight: 700;
  color: #1F3A8C;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.ai-subtitle {
  font-size: 15px;
  font-weight: 600;
  color: #4b5563;
  margin: 0;
  line-height: 1.5;
}

.ai-summary-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.summary-sections {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-section {
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.summary-section:last-child {
  padding-bottom: 0;
  border-bottom: none;
}

.section-header-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.section-icon {
  width: 18px;
  height: 18px;
  color: #1F3A8C;
  flex-shrink: 0;
}

.section-title-text {
  font-size: 14px;
  font-weight: 700;
  color: #1F3A8C;
}

.section-content {
  font-size: 14px;
  color: #374151;
  line-height: 1.7;
  margin: 0;
  padding-left: 26px;
  white-space: pre-line;
}

.ai-summary-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1F3A8C;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
}

.ai-summary-text {
  font-size: 14px;
  color: #1f2937;
  line-height: 1.8;
  margin: 0;
  white-space: pre-line;
}

/* 로딩 상태 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  gap: 16px;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #e0e7ff;
  border-top-color: #1F3A8C;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  font-weight: 500;
}

/* 메모 섹션 */
.memo-section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.section-title {
  font-size: 15px;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.memo-hint {
  font-size: 12px;
  color: #6b7280;
}

.memo-textarea {
  width: 100%;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px 14px;
  font-size: 14px;
  line-height: 1.6;
  resize: vertical;
  min-height: 100px;
  background: #ffffff;
  transition: all 0.2s ease;
}

.memo-textarea:focus {
  outline: none;
  border-color: #1F3A8C;
  box-shadow: 0 0 0 4px rgba(31, 58, 140, 0.1);
}
</style>
