<template>
  <DashboardLayout>
    <div class="h-full flex flex-col">
      <!-- 헤더 -->
      <div class="bg-white border-b border-gray-200 px-8 py-6">
        <div class="flex items-center justify-between mb-6">
          <div class="flex items-center gap-4">
            <div>
              <h1 class="text-2xl font-bold text-primary-600">Call History</h1>
              <p class="text-sm text-gray-500 mt-1">상담 이력을 확인하고 관리하세요</p>
            </div>
            <!-- 상담 상태 배지 -->
            <div
              class="status-badge px-4 py-2 rounded-lg shadow-sm transition-all duration-300"
              :data-active="dashboardStore.consultationStatus.isActive"
            >
              <div class="flex items-center text-white gap-2">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                </svg>
                <span class="text-xs font-bold">
                  상담 {{ dashboardStore.consultationStatus.isActive ? 'ON' : 'OFF' }}
                </span>
              </div>
            </div>
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

          <!-- 카테고리 필터 (Pill 버튼) -->
          <div class="flex items-center gap-2">
            <button
              v-for="category in categories"
              :key="category.value"
              @click="categoryFilter = category.value"
              :class="[
                'category-pill px-4 py-2 rounded-full text-sm font-semibold transition-all duration-200',
                categoryFilter === category.value
                  ? category.activeClass
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              ]"
            >
              {{ category.label }}
            </button>
          </div>

          <!-- 정렬 필터 -->
          <div class="relative">
            <select
              v-model="sortOrder"
              class="sort-select appearance-none px-5 py-2.5 pr-10 bg-white border-2 border-gray-200 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-all font-semibold text-sm text-gray-700 cursor-pointer hover:border-primary-300 shadow-sm"
              @change="handleSortChange"
            >
              <option value="latest">최신순</option>
              <option value="oldest">오래된 순</option>
              <option value="name">이름순</option>
              <option value="duration">통화시간순</option>
            </select>
            <svg class="absolute right-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-500 pointer-events-none" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
            </svg>
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
import { useDashboardStore } from '@/stores/dashboard'

const dashboardStore = useDashboardStore()

// 카테고리 정의 (아이콘 포함)
const categories = [
  { value: '', label: '전체', icon: '🏷️', activeClass: 'bg-primary-600 text-white shadow-md' },
  { value: 'REFRIGERATOR', label: '냉장고', icon: '🧊', activeClass: 'bg-blue-500 text-white shadow-md' },
  { value: 'WASHING_MACHINE', label: '세탁기', icon: '🌀', activeClass: 'bg-purple-500 text-white shadow-md' },
  { value: 'AIR_CONDITIONER', label: '에어컨', icon: '❄️', activeClass: 'bg-cyan-500 text-white shadow-md' },
  { value: 'TV', label: 'TV', icon: '📺', activeClass: 'bg-green-500 text-white shadow-md' },
  { value: 'OTHER', label: '기타', icon: '📦', activeClass: 'bg-gray-500 text-white shadow-md' }
]

// 카테고리 영문 → 한글 변환 함수
const getCategoryLabel = (category) => {
  const categoryMap = {
    'REFRIGERATOR': '냉장고',
    'WASHING_MACHINE': '세탁기',
    'AIR_CONDITIONER': '에어컨',
    'TV': 'TV',
    'OTHER': '기타'
  }
  return categoryMap[category] || category || '미분류'
}

// 정렬 옵션 정의
const sortOptions = [
  { value: 'latest', label: '최신순' },
  { value: 'oldest', label: '오래된 순' },
  { value: 'name', label: '이름순' },
  { value: 'duration', label: '통화시간순' }
]

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

<style scoped>
/* 상담 상태 배지 스타일 */
.status-badge {
  background-color: #6b7280; /* gray-500 - OFF 상태 */
}

.status-badge[data-active="true"] {
  background-color: #ef4444; /* red-500 - ON 상태 */
  animation: pulse-red 2s infinite;
}

@keyframes pulse-red {
  0% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.4); }
  70% { box-shadow: 0 0 0 10px rgba(239, 68, 68, 0); }
  100% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0); }
}

/* 카테고리 필터 pill 버튼 스타일 */
.category-pill {
  cursor: pointer;
  user-select: none;
}

.category-pill:active {
  transform: scale(0.95);
}

/* 정렬 필터 select 스타일 */
.sort-select {
  min-width: 150px;
}

.sort-select:hover {
  background-color: #fafafa;
}

.sort-select:focus {
  outline: none;
}
</style>
