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
              placeholder="고객명, 상담 내용으로 검색..."
              class="w-full px-4 py-2.5 pl-11 border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-all"
              @input="handleSearch"
            />
            <svg class="w-5 h-5 text-gray-400 absolute left-3.5 top-1/2 transform -translate-y-1/2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
            </svg>
          </div>

          <!-- 카테고리 필터 -->
          <select
            v-model="categoryFilter"
            class="px-4 py-2.5 border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-all font-medium text-gray-700"
            @change="handleFilterChange"
          >
            <option value="">전체 카테고리</option>
            <option value="냉장고">냉장고</option>
            <option value="세탁기">세탁기</option>
            <option value="에어컨">에어컨</option>
            <option value="TV">TV</option>
            <option value="기타">기타</option>
          </select>

          <!-- 정렬 버튼 -->
          <select
            v-model="sortOrder"
            class="px-4 py-2.5 border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-all font-medium text-gray-700"
            @change="handleSortChange"
          >
            <option value="latest">최신순</option>
            <option value="oldest">오래된 순</option>
            <option value="name">이름순</option>
            <option value="duration">통화시간 순</option>
          </select>
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

        <!-- 선택된 고객 표시 -->
        <div v-if="selectedCustomerId" class="mb-4 bg-primary-50 border border-primary-200 rounded-lg p-4 flex items-center justify-between">
          <div class="flex items-center gap-3">
            <svg class="w-5 h-5 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
            </svg>
            <span class="text-primary-800 font-medium">특정 고객의 상담 이력을 보고 있습니다.</span>
          </div>
          <button
            @click="handleCustomerClick(selectedCustomerId)"
            class="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-all font-medium"
          >
            전체 보기
          </button>
        </div>

        <!-- 테이블 -->
        <div>
          <CallHistoryTable
            :consultations="filteredConsultations"
            @customer-click="handleCustomerClick"
          />

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
import { getMyConsultations, getConsultationsByCustomer } from '@/services/consultationService'

const searchQuery = ref('')
const categoryFilter = ref('')
const sortOrder = ref('latest')
const selectedCustomerId = ref(null)
const isLoading = ref(false)
const error = ref(null)
const consultations = ref([])
const pagination = ref(null)

const filteredConsultations = computed(() => {
  let result = [...consultations.value]

  // 검색 필터링
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(c =>
      c.title?.toLowerCase().includes(query) ||
      c.subtitle?.toLowerCase().includes(query) ||
      c.customerName?.toLowerCase().includes(query)
    )
  }

  // 카테고리 필터링
  if (categoryFilter.value) {
    result = result.filter(c => c.productCategory === categoryFilter.value)
  }

  // 정렬
  switch (sortOrder.value) {
    case 'latest':
      result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      break
    case 'oldest':
      result.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
      break
    case 'name':
      result.sort((a, b) => a.customerName.localeCompare(b.customerName, 'ko'))
      break
    case 'duration':
      result.sort((a, b) => (b.durationSeconds || 0) - (a.durationSeconds || 0))
      break
  }

  return result
})

const handleFilterChange = () => {
  // 필터 변경 시 자동으로 computed가 재계산됨
}

const handleSortChange = () => {
  // 정렬 변경 시 자동으로 computed가 재계산됨
}

const handleSearch = () => {
  // 검색은 computed에서 자동으로 처리됨
}

// 고객 이름 클릭 시 해당 고객의 이력만 보기
const handleCustomerClick = async (customerId) => {
  if (selectedCustomerId.value === customerId) {
    // 이미 선택된 고객을 다시 클릭하면 전체 보기로 복귀
    selectedCustomerId.value = null
    await loadConsultations()
  } else {
    selectedCustomerId.value = customerId
    await loadCustomerConsultations(customerId)
  }
}

const loadCustomerConsultations = async (customerId, page = 0) => {
  isLoading.value = true
  error.value = null

  try {
    console.log('[CallHistoryView] 고객 상담 이력 조회, customerId:', customerId)
    const data = await getConsultationsByCustomer(customerId, page, 10)

    consultations.value = data.content
    pagination.value = {
      totalPages: data.totalPages,
      totalElements: data.totalElements,
      number: data.number,
      size: data.size,
      first: data.first,
      last: data.last
    }

    console.log('[CallHistoryView] 고객 상담 이력:', consultations.value)
  } catch (err) {
    console.error('[CallHistoryView] 고객 상담 이력 조회 실패:', err)
    error.value = '고객 상담 이력을 불러오는데 실패했습니다.'
  } finally {
    isLoading.value = false
  }
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
