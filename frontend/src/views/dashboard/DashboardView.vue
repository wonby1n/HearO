<template>
  <DashboardLayout>
    <!-- 헤더 -->
    <DashboardHeader />

    <!-- 메인 컨텐츠 그리드 -->
    <div class="container mx-auto px-6 py-6">
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 1번째 칸: 스트레스 지수 -->
        <div class="lg:col-span-1">
          <div class="h-[600px]">
            <StressChart />
          </div>
        </div>

        <!-- 2번째 칸: 주간 실적 차트 (위) + 통계 카드 2개 (아래) -->
        <div class="lg:col-span-1">
          <div class="flex flex-col gap-6 h-[600px]">
            <!-- 위: 주간 실적 -->
            <div class="flex-1">
              <WeeklyPerformanceChart />
            </div>

            <!-- 아래: 통계 카드 2개 -->
            <div class="grid grid-cols-2 gap-4">
              <!-- 총 상담 시간 -->
              <StatsCard
                icon="clock"
                title="총 상담 시간"
                :value="dashboardStore.formattedCallTime"
                color="purple"
              />

              <!-- 고객 만족도 -->
              <StatsCard
                icon="star"
                title="고객 만족도"
                :value="dashboardStore.customerSatisfaction"
                color="yellow"
              />
            </div>
          </div>
        </div>

        <!-- 3번째 칸: Todo List -->
        <div class="lg:col-span-1">
          <div class="h-[600px]">
            <TodoList />
          </div>
        </div>
      </div>
    </div>
  </DashboardLayout>
</template>

<script setup>
import { onMounted } from 'vue'
import { useDashboardStore } from '@/stores/dashboard'
import DashboardLayout from '@/components/layout/DashboardLayout.vue'
import DashboardHeader from '@/components/dashboard/DashboardHeader.vue'
import StressChart from '@/components/dashboard/StressChart.vue'
import WeeklyPerformanceChart from '@/components/dashboard/WeeklyPerformanceChart.vue'
import StatsCard from '@/components/dashboard/StatsCard.vue'
import TodoList from '@/components/dashboard/TodoList.vue'

const dashboardStore = useDashboardStore()

// 컴포넌트 마운트 시 대시보드 데이터 로드
onMounted(async () => {
  await dashboardStore.fetchDashboardData()
})
</script>

<style scoped>
/* DashboardView 전용 스타일 */
</style>
