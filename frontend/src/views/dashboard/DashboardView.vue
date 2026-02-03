<template>
  <DashboardLayout>
    <!-- 상단 헤더: 상담 상태 및 실시간 정보 -->
    <DashboardHeader ref="dashboardHeaderRef" />

    <div class="container mx-auto px-6 py-6">
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 1번째 섹션: 에너지 지수 -->
        <div class="lg:col-span-1">
          <div class="h-[600px]">
            <EnergyChart />
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

    <!-- 🔹 TimeModal 연결 (에너지 0일 때 의무 휴식) -->
    <TimeModal
      v-model="isTimeModalOpen"
      :duration="600"
      @complete="handleTimeModalComplete"
    />
  </DashboardLayout>
</template>

<script setup>
import { ref, onMounted, onUnmounted, onActivated, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useDashboardStore } from '@/stores/dashboard'
import { useAgentStore } from '@/stores/agent'
import DashboardLayout from '@/components/layout/DashboardLayout.vue'
import DashboardHeader from '@/components/dashboard/DashboardHeader.vue'
import EnergyChart from '@/components/dashboard/EnergyChart.vue'
import WeeklyPerformanceChart from '@/components/dashboard/WeeklyPerformanceChart.vue'
import StatsCard from '@/components/dashboard/StatsCard.vue'
import TodoList from '@/components/dashboard/TodoList.vue'
import MatchingModal from '@/components/dashboard/MatchingModal.vue'
import TimeModal from '@/components/dashboard/TimeModal.vue'

const router = useRouter()
const dashboardStore = useDashboardStore()
const agentStore = useAgentStore()

/**
 * 🔹 모달 상태 제어 변수
 * false: 닫힘, true: 열림
 */
const isModalOpen = ref(false)
const isTimeModalOpen = ref(false)

// DashboardHeader 컴포넌트 ref
const dashboardHeaderRef = ref(null)

// 매칭 데이터 감지하여 모달 열기
watch(
  () => dashboardStore.matchedData,
  (newData) => {
    if (newData) {
      isModalOpen.value = true
    }
  }
)

// 에너지 레벨 감지하여 0이 되면 TimeModal 열기
watch(
  () => agentStore.energyLevel,
  (newLevel, oldLevel) => {

    if (newLevel !== null && newLevel <= 0 && !isTimeModalOpen.value) {
      console.log('[DashboardView] 에너지 0 이하 감지 - TimeModal 열기')
      isTimeModalOpen.value = true

      // 상담 상태 강제로 OFF (의무 휴식)
      if (dashboardStore.consultationStatus.isActive) {
        console.log('[DashboardView] 상담 모드 강제 OFF')
        dashboardStore.consultationStatus.isActive = false
      }
    }
  }
)

// 모달 닫기 시 LiveKit 연결 후 통화 화면으로 이동
const handleModalClose = async () => {
  isModalOpen.value = false

  try {
    // 상담사가 확인했으므로 이제 LiveKit에 연결
    console.log('[DashboardView] 상담사 확인 - LiveKit 연결 시작')
    await dashboardHeaderRef.value?.connectToCall()
    console.log('[DashboardView] LiveKit 연결 완료 - 통화 화면으로 이동')

    // matchedData는 통화 중에 필요하므로 여기서 지우지 않음
    // 통화 종료 후 대시보드로 돌아올 때 정리됨
    router.push('/counselor/call')
  } catch (error) {
    console.error('[DashboardView] LiveKit 연결 실패:', error)
    // 에러 발생 시에도 통화 화면으로 이동 (CounselorCallView에서 재시도)
    router.push('/counselor/call')
  }
}

// TimeModal 완료 시 자동으로 닫기
const handleTimeModalComplete = () => {
  console.log('[DashboardView] TimeModal 10분 완료 - 자동 닫기')
  isTimeModalOpen.value = false
}

let energyLogInterval = null

onMounted(async () => {
  console.log('[DashboardView] onMounted - 데이터 로딩')
  await dashboardStore.fetchDashboardData()
  console.log('[DashboardView] 고객 만족도:', dashboardStore.customerSatisfaction)

  // 30초마다 에너지 레벨 콘솔 출력 (디버깅용)
  energyLogInterval = setInterval(() => {
  }, 30000)
})

// keep-alive 캐시 컴포넌트가 다시 활성화될 때 데이터 갱신
onActivated(async () => {
  console.log('[DashboardView] onActivated - 데이터 갱신')
  await dashboardStore.fetchDashboardData()
  console.log('[DashboardView] 고객 만족도:', dashboardStore.customerSatisfaction)
})

onUnmounted(() => {
  if (energyLogInterval) {
    clearInterval(energyLogInterval)
  }
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