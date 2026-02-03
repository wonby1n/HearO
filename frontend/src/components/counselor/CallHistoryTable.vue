<template>
  <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
    <!-- 테이블 헤더 -->
    <div class="grid grid-cols-12 gap-4 px-6 py-4 border-b border-gray-200 bg-gradient-to-r from-primary-50 to-primary-100/50 text-sm font-semibold text-primary-800">
      <div class="col-span-2">제품 카테고리</div>
      <div class="col-span-2">고객명</div>
      <div class="col-span-2">전화번호</div>
      <div class="col-span-3">접수내역 (AI 요약)</div>
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
          <div class="col-span-2 flex items-center text-sm font-medium text-gray-900">
            {{ consultation.customerName }}
          </div>
          <div class="col-span-2 flex items-center text-sm text-gray-600">
            {{ formatPhoneNumber(consultation.customerPhone) }}
          </div>
          <div class="col-span-3 flex items-center text-sm text-gray-700">
            <div class="truncate" :title="consultation.aiSummary">
              {{ truncateText(consultation.aiSummary, 60) }}
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
          class="px-6 py-4 bg-gradient-to-br from-primary-50/30 to-blue-50/30 border-t border-primary-100"
        >
          <div class="grid grid-cols-1 gap-4 mb-4">
            <!-- AI 요약 전체 보기 -->
            <div>
              <h4 class="text-sm font-semibold text-primary-800 mb-3 flex items-center gap-2">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
                </svg>
                접수내역 (AI 요약)
              </h4>
              <div class="bg-white rounded-lg border border-primary-200 p-4 text-sm shadow-sm">
                <p class="text-gray-700 whitespace-pre-wrap leading-relaxed">{{ consultation.aiSummary || '요약 정보가 없습니다.' }}</p>
              </div>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <!-- 상담 결과 -->
            <div>
              <h4 class="text-sm font-semibold text-primary-800 mb-3 flex items-center gap-2">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
                </svg>
                상담 결과
              </h4>
              <div class="bg-white rounded-lg border border-primary-200 p-4 space-y-3 text-sm shadow-sm">
                <div class="flex justify-between items-center">
                  <span class="text-gray-600">제품 카테고리:</span>
                  <span class="px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-xs font-semibold">
                    {{ consultation.productCategory || '미분류' }}
                  </span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-gray-600">종료 사유:</span>
                  <span class="px-3 py-1 bg-primary-100 text-primary-700 rounded-full text-xs font-semibold">
                    {{ getTerminationReasonText(consultation.terminationReason) }}
                  </span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-gray-600">욕설 횟수:</span>
                  <span class="text-gray-900 font-semibold">{{ consultation.profanityCount }}회</span>
                </div>
                <div v-if="consultation.avgAggressionScore !== null" class="flex justify-between items-center">
                  <span class="text-gray-600">평균 공격성:</span>
                  <span class="text-gray-900 font-semibold">{{ consultation.avgAggressionScore.toFixed(1) }}</span>
                </div>
              </div>
            </div>

            <!-- 고객 평가 -->
            <div v-if="consultation.rating">
              <h4 class="text-sm font-semibold text-primary-800 mb-3 flex items-center gap-2">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z"/>
                </svg>
                고객 평가
              </h4>
              <div class="bg-white rounded-lg border border-primary-200 p-4 space-y-3 text-sm shadow-sm">
                <div class="flex justify-between items-center">
                  <span class="text-gray-600">처리 속도:</span>
                  <span class="text-primary-700 font-semibold flex items-center gap-1">
                    <svg class="w-4 h-4 text-yellow-500" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                    </svg>
                    {{ consultation.rating.processRating }}점
                  </span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-gray-600">해결 만족도:</span>
                  <span class="text-primary-700 font-semibold flex items-center gap-1">
                    <svg class="w-4 h-4 text-yellow-500" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                    </svg>
                    {{ consultation.rating.solutionRating }}점
                  </span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-gray-600">친절도:</span>
                  <span class="text-primary-700 font-semibold flex items-center gap-1">
                    <svg class="w-4 h-4 text-yellow-500" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                    </svg>
                    {{ consultation.rating.kindnessRating }}점
                  </span>
                </div>
                <div v-if="consultation.rating.feedback" class="pt-3 mt-1 border-t border-primary-100">
                  <span class="text-gray-600 text-xs font-medium">피드백</span>
                  <p class="text-gray-800 mt-2 text-sm leading-relaxed bg-primary-50/50 p-3 rounded-lg">{{ consultation.rating.feedback }}</p>
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
import { ref } from 'vue'

defineProps({
  consultations: {
    type: Array,
    required: true
  }
})

const expandedId = ref(null)

const toggleExpand = (id) => {
  expandedId.value = expandedId.value === id ? null : id
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
    ERROR: '오류'
  }
  return reasonMap[reason] || reason
}
</script>
