<template>
  <DashboardLayout>
    <!-- 상단 헤더: 상담 상태 및 실시간 정보 -->
    <DashboardHeader />

    <div class="container mx-auto px-6 py-6">
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 1번째 섹션: 스트레스 지수 -->
        <div class="lg:col-span-1">
          <div class="h-[600px]">
            <StressChart />
          </div>
        </div>

        <!-- 2번째 섹션: 주간 실적 차트 + 통계 카드 -->
        <div class="lg:col-span-1">
          <div class="flex flex-col gap-6 h-[600px]">
            <div class="flex-1">
              <WeeklyPerformanceChart />
            </div>

            <div class="grid grid-cols-2 gap-4">
              <StatsCard
                icon="clock"
                title="총 상담 시간"
                :value="dashboardStore.formattedCallTime"
                color="purple"
              />
              <StatsCard
                icon="star"
                title="고객 만족도"
                :value="dashboardStore.customerSatisfaction"
                color="yellow"
              />
            </div>
          </div>
        </div>

        <!-- 3번째 섹션: 할 일 목록 -->
        <div class="lg:col-span-1">
          <div class="h-[600px]">
            <TodoList />
          </div>
        </div>
      </div>
      
      <!-- [추가] 모달 테스트 버튼: 클릭 시 모달이 열립니다. -->
      <!-- <div class="mt-8 flex justify-center">
        <button 
          @click="isModalOpen = true"
          class="px-8 py-4 bg-[#1F3A8C] text-white rounded-2xl font-bold hover:bg-[#162a65] transition-all shadow-xl active:scale-95 flex items-center gap-3"
        >
          <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
            <path d="M12 2L4 5v6.09c0 5.05 3.41 9.76 8 10.91 4.59-1.15 8-5.86 8-10.91V5l-8-3z"/>
          </svg>
          AI 매칭 시작하기
        </button>
      </div> -->
    </div>

    <!-- 🔹 MatchingModal 연결 -->
    <!-- isOpen 프로퍼티와 close 이벤트를 바인딩했습니다. -->
    <MatchingModal
      :is-open="isModalOpen"
      @close="handleModalClose"
    />
  </DashboardLayout>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useDashboardStore } from '@/stores/dashboard'
import DashboardLayout from '@/components/layout/DashboardLayout.vue'
import DashboardHeader from '@/components/dashboard/DashboardHeader.vue'
import StressChart from '@/components/dashboard/StressChart.vue'
import WeeklyPerformanceChart from '@/components/dashboard/WeeklyPerformanceChart.vue'
import StatsCard from '@/components/dashboard/StatsCard.vue'
import TodoList from '@/components/dashboard/TodoList.vue'
import MatchingModal from '@/components/dashboard/MatchingModal.vue'

const router = useRouter()
const dashboardStore = useDashboardStore()

/**
 * 🔹 모달 상태 제어 변수
 * false: 닫힘, true: 열림
 */
const isModalOpen = ref(false)

// 매칭 데이터 감지하여 모달 열기
watch(
  () => dashboardStore.matchedData,
  (newData) => {
    if (newData) {
      isModalOpen.value = true
    }
  }
)

// 모달 닫기 시 통화 화면으로 이동
const handleModalClose = () => {
  isModalOpen.value = false
  dashboardStore.clearMatchedData()
  router.push('/counselor/call')
}

onMounted(async () => {
  await dashboardStore.fetchDashboardData()
})
</script>

<style scoped>
.container {
  animation: fadeIn 0.8s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>