<template>
  <div class="card h-full flex flex-col">
    <h3 class="text-lg font-semibold mb-4">Todo List</h3>

    <!-- Todo 항목 리스트 -->
    <div class="flex-1 overflow-y-auto scrollbar-thin">
      <ul class="space-y-3">
        <li
          v-for="todo in dashboardStore.todos"
          :key="todo.id"
          class="py-3 border-b border-gray-100 last:border-b-0"
        >
          <!-- 체크박스와 텍스트를 label로 감싸기 -->
          <label class="flex items-center gap-3 cursor-pointer">
            <!-- 체크박스 -->
            <input
              type="checkbox"
              :id="`todo-${todo.id}`"
              :checked="todo.completed"
              @change="handleToggle(todo.id)"
              class="w-5 h-5 text-primary-600 bg-gray-100 border-gray-300 rounded focus:ring-primary-500 focus:ring-2 cursor-pointer"
            />

            <!-- Todo 텍스트 -->
            <span
              :class="[
                'flex-1 text-gray-900',
                todo.completed && 'line-through opacity-60'
              ]"
            >
              {{ todo.text }}
            </span>
          </label>
        </li>
      </ul>

      <!-- Todo 항목이 없을 때 -->
      <div v-if="dashboardStore.todos.length === 0" class="text-center py-8 text-gray-500">
        <p>등록된 Todo가 없습니다.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useDashboardStore } from '@/stores/dashboard'

const dashboardStore = useDashboardStore()

// Todo 체크 토글
const handleToggle = (id) => {
  dashboardStore.toggleTodo(id)
}
</script>

<style scoped>
/* TodoList 전용 스타일 */
</style>
