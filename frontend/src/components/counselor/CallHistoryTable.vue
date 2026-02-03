<template>
  <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
    <!-- 테이블 헤더 -->
    <div class="grid grid-cols-12 gap-4 px-6 py-4 border-b border-gray-200 bg-gradient-to-r from-primary-50 to-primary-100/50 text-sm font-semibold text-primary-800">
      <div class="col-span-2">제품 카테고리</div>
      <div class="col-span-2">고객명</div>
      <div class="col-span-2">전화번호</div>
      <div class="col-span-3">접수내역</div>
      <div class="col-span-2">통화일자</div>
      <div class="col-span-1 text-right">총통화시간</div>
    </div>

    <!-- 테이블 바디 -->
    <div v-if="consultations.length === 0" class="px-6 py-12 text-center text-gray-500">
      상담 이력이 없습니다.
    </div>

    <div v-else>
      <div
        v-for="consultation in consultations"
        :key="consultation.consultationId"
        class="border-b border-gray-200 last:border-b-0"
      >
        <!-- 기본 행 -->
        <div
          class="grid grid-cols-12 gap-4 px-6 py-4 hover:bg-primary-50/50 cursor-pointer transition-all duration-200"
          @click="toggleExpand(consultation.consultationId)"
        >
          <div class="col-span-2 flex items-center">
            <span class="px-3 py-1.5 bg-primary-100 text-primary-700 rounded-lg text-sm font-medium">
              {{ consultation.productCategory || '미분류' }}
            </span>
          </div>
          <div class="col-span-2 flex items-center text-sm">
            <button
              @click.stop="handleCustomerClick(consultation.customerId)"
              class="font-medium text-primary-700 hover:text-primary-900 hover:underline transition-all"
            >
              {{ consultation.customerName }}
            </button>
          </div>
          <div class="col-span-2 flex items-center text-sm text-gray-600">
            {{ formatPhoneNumber(consultation.customerPhone) }}
          </div>
          <div class="col-span-3 flex items-center text-sm text-gray-700">
            <div class="truncate" :title="consultation.title">
              {{ truncateText(consultation.title, 60) }}
            </div>
          </div>
          <div class="col-span-2 flex items-center text-sm text-gray-600">
            {{ formatDate(consultation.createdAt) }}
          </div>
          <div class="col-span-1 flex items-center justify-end text-sm text-gray-900 font-medium">
            {{ formatDuration(consultation.durationSeconds) }}
          </div>
        </div>

        <!-- 확장 영역 -->
        <div
          v-if="expandedId === consultation.consultationId"
          class="border-t border-primary-100 bg-gradient-to-br from-primary-50/30 to-blue-50/30"
        >
          <!-- 탭 네비게이션 -->
          <div class="flex border-b border-primary-200 bg-white/50">
            <button
              v-for="tab in tabs"
              :key="tab.id"
              @click="activeTab[consultation.consultationId] = tab.id"
              :class="[
                'px-6 py-3 text-sm font-medium transition-all relative',
                activeTab[consultation.consultationId] === tab.id
                  ? 'text-primary-700 bg-white'
                  : 'text-gray-600 hover:text-primary-600 hover:bg-white/30'
              ]"
            >
              <div class="flex items-center gap-2">
                <component :is="tab.icon" class="w-4 h-4" />
                <span>{{ tab.label }}</span>
              </div>
              <div
                v-if="activeTab[consultation.consultationId] === tab.id"
                class="absolute bottom-0 left-0 right-0 h-0.5 bg-primary-600"
              ></div>
            </button>
          </div>

          <!-- 탭 컨텐츠 -->
          <div class="px-6 py-4">
            <!-- AI 요약 탭 -->
            <div v-if="activeTab[consultation.consultationId] === 'ai-summary'">
              <div class="bg-white rounded-lg border border-primary-200 p-4 shadow-sm">
                <p class="text-sm text-gray-700 whitespace-pre-wrap leading-relaxed">
                  {{ consultation.aiSummary || 'AI 요약 정보가 없습니다.' }}
                </p>
              </div>
            </div>

            <!-- STT 전문 탭 -->
            <div v-if="activeTab[consultation.consultationId] === 'transcript'">
              <div class="bg-white rounded-lg border border-primary-200 p-4 shadow-sm max-h-96 overflow-y-auto">
                <p class="text-sm text-gray-700 whitespace-pre-wrap leading-relaxed">
                  {{ consultation.fullTranscript || 'STT 저장본이 없습니다.' }}
                </p>
              </div>
            </div>

            <!-- 접수 정보 탭 (고객이 접수한 정보만) -->
            <div v-if="activeTab[consultation.consultationId] === 'registration'">
              <div class="bg-white rounded-lg border border-primary-200 p-4 shadow-sm space-y-4">
                <div class="border-b border-gray-200 pb-2 mb-3">
                  <h4 class="text-sm font-bold text-gray-900 flex items-center gap-2">
                    <svg class="w-4 h-4 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
                    </svg>
                    고객이 접수 시 입력한 정보
                  </h4>
                </div>
                <div>
                  <h5 class="text-xs font-semibold text-gray-600 mb-2">증상 설명</h5>
                  <p class="text-sm text-gray-900 bg-gray-50 p-3 rounded-lg leading-relaxed">
                    {{ consultation.symptom || '증상 정보가 없습니다.' }}
                  </p>
                </div>
                <div v-if="consultation.errorCode">
                  <h5 class="text-xs font-semibold text-gray-600 mb-2">에러 코드</h5>
                  <div class="inline-flex items-center gap-2 bg-red-50 px-3 py-2 rounded-lg">
                    <svg class="w-4 h-4 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                    </svg>
                    <span class="text-sm font-mono font-semibold text-red-700">{{ consultation.errorCode }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 상담사 메모 탭 (상담사가 작성한 메모) -->
            <div v-if="activeTab[consultation.consultationId] === 'counselor-memo'">
              <div class="bg-white rounded-lg border border-blue-200 p-4 shadow-sm">
                <div class="border-b border-blue-200 pb-2 mb-3">
                  <h4 class="text-sm font-bold text-gray-900 flex items-center gap-2">
                    <svg class="w-4 h-4 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                    </svg>
                    통화 중 작성한 메모
                  </h4>
                </div>
                <div v-if="consultation.userMemo">
                  <p class="text-sm text-gray-900 bg-blue-50 p-4 rounded-lg whitespace-pre-wrap leading-relaxed">
                    {{ consultation.userMemo }}
                  </p>
                </div>
                <div v-else class="text-sm text-gray-500 text-center py-8 bg-gray-50 rounded-lg">
                  작성된 메모가 없습니다.
                </div>
              </div>
            </div>

            <!-- 상담 결과 탭 -->
            <div v-if="activeTab[consultation.consultationId] === 'result'">
              <div class="grid grid-cols-2 gap-4">
                <!-- 상담 통계 -->
                <div class="bg-white rounded-lg border border-primary-200 p-4 shadow-sm space-y-3">
                  <h5 class="text-sm font-semibold text-gray-800 mb-3">상담 통계</h5>
                  <div class="flex justify-between items-center text-sm">
                    <span class="text-gray-600">제품 카테고리:</span>
                    <span class="px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-xs font-semibold">
                      {{ consultation.productCategory || '미분류' }}
                    </span>
                  </div>
                  <div class="flex justify-between items-center text-sm">
                    <span class="text-gray-600">종료 사유:</span>
                    <span class="px-3 py-1 bg-primary-100 text-primary-700 rounded-full text-xs font-semibold">
                      {{ getTerminationReasonText(consultation.terminationReason) }}
                    </span>
                  </div>
                  <div class="flex justify-between items-center text-sm">
                    <span class="text-gray-600">욕설 횟수:</span>
                    <span class="text-gray-900 font-semibold">{{ consultation.profanityCount }}회</span>
                  </div>
                  <div v-if="consultation.avgAggressionScore !== null" class="flex justify-between items-center text-sm">
                    <span class="text-gray-600">평균 공격성:</span>
                    <span class="text-gray-900 font-semibold">{{ consultation.avgAggressionScore.toFixed(1) }}</span>
                  </div>
                </div>

                <!-- 고객 평가 (간소화) -->
                <div class="bg-white rounded-lg border border-primary-200 p-4 shadow-sm">
                  <h5 class="text-sm font-semibold text-gray-800 mb-3 flex items-center gap-2">
                    <svg class="w-4 h-4 text-yellow-500" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                    </svg>
                    고객 평가
                  </h5>
                  <div v-if="consultation.rating" class="space-y-2 text-sm">
                    <div class="flex justify-between items-center">
                      <span class="text-gray-600">처리 속도:</span>
                      <span class="text-yellow-600 font-semibold">★ {{ consultation.rating.processRating }}</span>
                    </div>
                    <div class="flex justify-between items-center">
                      <span class="text-gray-600">해결 만족도:</span>
                      <span class="text-yellow-600 font-semibold">★ {{ consultation.rating.solutionRating }}</span>
                    </div>
                    <div class="flex justify-between items-center">
                      <span class="text-gray-600">친절도:</span>
                      <span class="text-yellow-600 font-semibold">★ {{ consultation.rating.kindnessRating }}</span>
                    </div>
                    <div v-if="consultation.rating.feedback" class="pt-3 mt-3 border-t border-gray-200">
                      <p class="text-xs text-gray-600 italic">"{{ consultation.rating.feedback }}"</p>
                    </div>
                  </div>
                  <div v-else class="text-sm text-gray-500 text-center py-4">
                    평가 정보가 없습니다.
                  </div>
                </div>
              </div>

              <!-- 녹음 파일 재생 -->
              <div v-if="consultation.voiceRecording" class="mt-4 bg-white rounded-lg border border-primary-200 p-4 shadow-sm">
                <h5 class="text-sm font-semibold text-gray-800 mb-3 flex items-center gap-2">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.536 8.464a5 5 0 010 7.072m2.828-9.9a9 9 0 010 12.728M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z"/>
                  </svg>
                  통화 녹음
                </h5>
                <audio
                  controls
                  class="w-full"
                  :src="`https://i14e106.p.ssafy.io${consultation.voiceRecording.fileUrl}`"
                >
                  브라우저가 오디오 재생을 지원하지 않습니다.
                </audio>
                <div class="mt-2 flex items-center justify-between text-xs text-gray-500">
                  <span>파일 크기: {{ formatFileSize(consultation.voiceRecording.fileSize) }}</span>
                  <span>길이: {{ formatDuration(consultation.voiceRecording.durationSeconds) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, h } from 'vue'

defineProps({
  consultations: {
    type: Array,
    required: true
  }
})

const emit = defineEmits(['customer-click'])

const expandedId = ref(null)
const activeTab = ref({})

const handleCustomerClick = (customerId) => {
  emit('customer-click', customerId)
}

// 탭 정의
const tabs = [
  {
    id: 'ai-summary',
    label: 'AI 요약',
    icon: () => h('svg', { class: 'w-4 h-4', fill: 'none', stroke: 'currentColor', viewBox: '0 0 24 24' }, [
      h('path', { 'stroke-linecap': 'round', 'stroke-linejoin': 'round', 'stroke-width': '2', d: 'M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z' })
    ])
  },
  {
    id: 'transcript',
    label: 'STT 전문',
    icon: () => h('svg', { class: 'w-4 h-4', fill: 'none', stroke: 'currentColor', viewBox: '0 0 24 24' }, [
      h('path', { 'stroke-linecap': 'round', 'stroke-linejoin': 'round', 'stroke-width': '2', d: 'M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z' })
    ])
  },
  {
    id: 'registration',
    label: '접수 정보',
    icon: () => h('svg', { class: 'w-4 h-4', fill: 'none', stroke: 'currentColor', viewBox: '0 0 24 24' }, [
      h('path', { 'stroke-linecap': 'round', 'stroke-linejoin': 'round', 'stroke-width': '2', d: 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z' })
    ])
  },
  {
    id: 'counselor-memo',
    label: '상담사 메모',
    icon: () => h('svg', { class: 'w-4 h-4', fill: 'none', stroke: 'currentColor', viewBox: '0 0 24 24' }, [
      h('path', { 'stroke-linecap': 'round', 'stroke-linejoin': 'round', 'stroke-width': '2', d: 'M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z' })
    ])
  },
  {
    id: 'result',
    label: '상담 결과',
    icon: () => h('svg', { class: 'w-4 h-4', fill: 'none', stroke: 'currentColor', viewBox: '0 0 24 24' }, [
      h('path', { 'stroke-linecap': 'round', 'stroke-linejoin': 'round', 'stroke-width': '2', d: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2' })
    ])
  }
]

const toggleExpand = (id) => {
  if (expandedId.value === id) {
    expandedId.value = null
  } else {
    expandedId.value = id
    // 탭이 없으면 첫 번째 탭(AI 요약)으로 초기화
    if (!activeTab.value[id]) {
      activeTab.value[id] = 'ai-summary'
    }
  }
}

const truncateText = (text, maxLength) => {
  if (!text) return '-'
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

const formatPhoneNumber = (phone) => {
  if (!phone) return '-'
  // 전화번호 마스킹 (중간 4자리)
  return phone.replace(/(\d{3})-?(\d{4})-?(\d{4})/, '$1-****-$3')
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).replace(/\. /g, '-').replace('.', '')
}

const formatDuration = (seconds) => {
  if (seconds === null || seconds === undefined) return '-'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}

const getTerminationReasonText = (reason) => {
  const reasonMap = {
    NORMAL: '정상 종료',
    CUSTOMER_DISCONNECT: '고객 종료',
    COUNSELOR_DISCONNECT: '상담사 종료',
    BLACKLIST: '블랙리스트',
    TIMEOUT: '시간 초과',
    ERROR: '오류',
    PROFANITY_LIMIT: '욕설 제한',
    AGGRESSION_LIMIT: '공격성 제한'
  }
  return reasonMap[reason] || reason
}

const formatFileSize = (bytes) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}
</script>
