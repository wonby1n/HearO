<template>
  <DashboardLayout>
    <div class="h-full flex flex-col">
      <!-- 헤더 -->
      <div class="bg-white border-b border-gray-200 px-8 py-6">
        <div class="flex items-center justify-between mb-6">
          <div>
            <h1 class="text-2xl font-bold text-primary-600">Call History</h1>
            <p class="text-sm text-gray-500 mt-1">상담 이력을 확인하고 관리하세요</p>
          </div>
          <div class="flex items-center gap-3">
            <div class="bg-primary-50 px-4 py-2 rounded-lg">
              <span class="text-sm text-primary-600 font-semibold">총 {{ pagination?.totalElements || 0 }}건</span>
            </div>
          </div>
        </div>

        <!-- 검색 및 필터 -->
        <div class="flex gap-3">
          <!-- 검색바 -->
          <div class="flex-1 relative">
            <input
              v-model="searchQuery"
              type="text"
              placeholder="고객명, 상담 제목으로 검색..."
              class="w-full px-4 py-2.5 pl-11 border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-all"
              @input="handleSearch"
            />
            <svg class="w-5 h-5 text-gray-400 absolute left-3.5 top-1/2 transform -translate-y-1/2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
            </svg>
          </div>

          <!-- 필터 버튼 -->
          <button
            class="px-5 py-2.5 border border-gray-300 rounded-xl hover:bg-gray-50 transition-all flex items-center gap-2 font-medium text-gray-700"
            @click="toggleFilter"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"/>
            </svg>
            <span>필터</span>
            <span v-if="activeFilters.length > 0" class="bg-primary-600 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center font-semibold">
              {{ activeFilters.length }}
            </span>
          </button>

          <!-- 정렬 버튼 -->
          <button class="px-5 py-2.5 border border-gray-300 rounded-xl hover:bg-gray-50 transition-all flex items-center gap-2 font-medium text-gray-700">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4h13M3 8h9m-9 4h6m4 0l4-4m0 0l4 4m-4-4v12"/>
            </svg>
            <span>최신순</span>
          </button>
        </div>

        <!-- 활성 필터 태그 -->
        <div v-if="activeFilters.length > 0" class="flex flex-wrap gap-2 mt-4">
          <div
            v-for="filter in activeFilters"
            :key="filter"
            class="inline-flex items-center gap-2 px-3 py-1.5 bg-primary-100 text-primary-700 rounded-lg text-sm font-medium"
          >
            <span>{{ filter }}</span>
            <button @click="removeFilter(filter)" class="hover:text-primary-900 transition-colors">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- 메인 컨텐츠 -->
      <div class="flex-1 overflow-y-auto px-8 py-6">
        <!-- 로딩 상태 -->
        <div v-if="isLoading" class="flex flex-col items-center justify-center py-20">
          <div class="animate-spin rounded-full h-12 w-12 border-4 border-primary-200 border-t-primary-600"></div>
          <p class="mt-4 text-gray-500 text-sm">상담 이력을 불러오는 중...</p>
        </div>

        <!-- 에러 상태 -->
        <div v-else-if="error" class="bg-red-50 border-l-4 border-red-500 rounded-lg p-6">
          <div class="flex items-start gap-3">
            <svg class="w-6 h-6 text-red-500 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
            <div>
              <h3 class="text-red-800 font-semibold mb-1">오류가 발생했습니다</h3>
              <p class="text-red-700 text-sm">{{ error }}</p>
            </div>
          </div>
        </div>

        <!-- 테이블 -->
        <div v-else>
          <CallHistoryTable :consultations="filteredConsultations" />

          <!-- 페이지네이션 -->
          <div v-if="pagination && pagination.totalPages > 1" class="mt-8 flex justify-center items-center gap-3">
            <button
              :disabled="pagination.first"
              @click="goToPage(pagination.number - 1)"
              class="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-all font-medium text-gray-700"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
              </svg>
            </button>

            <div class="flex items-center gap-2 px-6 py-2 bg-primary-50 rounded-lg">
              <span class="text-sm font-semibold text-primary-700">
                {{ pagination.number + 1 }}
              </span>
              <span class="text-sm text-primary-400">/</span>
              <span class="text-sm font-medium text-primary-600">
                {{ pagination.totalPages }}
              </span>
            </div>

            <button
              :disabled="pagination.last"
              @click="goToPage(pagination.number + 1)"
              class="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-all font-medium text-gray-700"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </DashboardLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import DashboardLayout from '@/components/layout/DashboardLayout.vue'
import CallHistoryTable from '@/components/counselor/CallHistoryTable.vue'
import { getMyConsultations } from '@/services/consultationService'

const searchQuery = ref('')
const activeFilters = ref([])
const isLoading = ref(false)
const error = ref(null)
const consultations = ref([])
const pagination = ref(null)

const filteredConsultations = computed(() => {
  let result = consultations.value

  // 검색 필터링
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(c =>
      c.title?.toLowerCase().includes(query) ||
      c.subtitle?.toLowerCase().includes(query) ||
      c.customerName?.toLowerCase().includes(query)
    )
  }

  return result
})

const toggleFilter = () => {
  // TODO: 필터 모달 열기
  console.log('필터 열기')
}

const removeFilter = (filter) => {
  activeFilters.value = activeFilters.value.filter(f => f !== filter)
}

const handleSearch = () => {
  // 검색은 computed에서 자동으로 처리됨
}

const loadConsultations = async (page = 0) => {
  isLoading.value = true
  error.value = null

  try {
    console.log('[CallHistoryView] 상담 이력 조회, page:', page)
    const data = await getMyConsultations(page, 10)

    consultations.value = data.content
    pagination.value = {
      totalPages: data.totalPages,
      totalElements: data.totalElements,
      number: data.number,
      size: data.size,
      first: data.first,
      last: data.last
    }

    console.log('[CallHistoryView] 상담 이력:', consultations.value)
  } catch (err) {
    console.error('[CallHistoryView] 상담 이력 조회 실패:', err)
    error.value = '상담 이력을 불러오는데 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

const goToPage = (page) => {
  loadConsultations(page)
}

onMounted(() => {
  loadConsultations()
})
</script>
