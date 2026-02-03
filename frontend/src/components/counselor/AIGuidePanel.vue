<template>
  <div class="h-full flex flex-col">
    <!-- 검색 입력 -->
    <div class="mb-4">
      <div class="flex gap-2">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="에러코드 입력 (예: E101)"
          class="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 text-sm"
          @keypress.enter="handleSearch"
        />
        <button
          @click="handleSearch"
          :disabled="isLoading || !searchQuery.trim()"
          class="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors text-sm font-medium"
        >
          <span v-if="isLoading">검색 중...</span>
          <span v-else>검색</span>
        </button>
      </div>
    </div>

    <!-- 결과 표시 영역 -->
    <div class="flex-1 overflow-y-auto">
      <!-- 에러 상태 -->
      <div v-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 text-sm text-red-700">
        {{ error }}
      </div>

      <!-- 로딩 상태 -->
      <div v-else-if="isLoading" class="flex items-center justify-center py-8">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>

      <!-- 결과 표시 -->
      <div v-else-if="result" class="space-y-4">
        <!-- 답변 -->
        <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <h4 class="text-sm font-semibold text-blue-900 mb-2 flex items-center gap-2">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            답변
          </h4>
          <p class="text-sm text-gray-800 leading-relaxed">{{ result.answer }}</p>
        </div>

        <!-- 근거 자료 (Evidence) -->
        <div v-if="result.evidence && result.evidence.length > 0" class="bg-gray-50 border border-gray-200 rounded-lg p-4">
          <h4 class="text-sm font-semibold text-gray-900 mb-2 flex items-center gap-2">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            근거 자료
          </h4>
          <div class="space-y-2">
            <div
              v-for="(item, index) in result.evidence"
              :key="index"
              class="bg-white rounded p-3 border border-gray-200"
            >
              <div class="text-xs text-gray-500 mb-1">출처: {{ item.source }}</div>
              <p class="text-sm text-gray-700 italic">"{{ item.quote }}"</p>
            </div>
          </div>
        </div>

        <!-- 추가 정보 필요 -->
        <div v-if="result.needMoreInfo && result.needMoreInfo.length > 0" class="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
          <h4 class="text-sm font-semibold text-yellow-900 mb-2 flex items-center gap-2">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            추가 정보 필요
          </h4>
          <ul class="list-disc list-inside space-y-1">
            <li v-for="(info, index) in result.needMoreInfo" :key="index" class="text-sm text-gray-700">
              {{ info }}
            </li>
          </ul>
        </div>
      </div>

      <!-- 초기 상태 -->
      <div v-else class="text-center py-8 text-gray-500 text-sm">
        <svg class="w-12 h-12 mx-auto mb-3 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
        </svg>
        <p>에러코드를 입력하고 검색하세요</p>
        <p class="text-xs mt-1 text-gray-400">예: E101, E202 등</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { askErrorCode } from '@/services/ragService'

const searchQuery = ref('')
const isLoading = ref(false)
const error = ref(null)
const result = ref(null)

const handleSearch = async () => {
  const query = searchQuery.value.trim()
  if (!query) return

  isLoading.value = true
  error.value = null
  result.value = null

  try {
    console.log('[AIGuidePanel] 에러코드 검색:', query)
    const response = await askErrorCode(query)
    result.value = response
    console.log('[AIGuidePanel] AI 답변:', response)
  } catch (err) {
    console.error('[AIGuidePanel] 검색 실패:', err)
    error.value = 'AI 답변을 가져오는데 실패했습니다. 다시 시도해주세요.'
  } finally {
    isLoading.value = false
  }
}
</script>
