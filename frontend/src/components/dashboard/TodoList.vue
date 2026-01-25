<template>
  <div class="card h-full flex flex-col">
    <h3 class="text-lg font-semibold mb-4">Todo List</h3>

    <!-- Todo 추가 입력창 -->
    <div class="mb-4">
      <form @submit.prevent="handleAddTodo" class="flex gap-2">
        <input
          v-model="newTodoText"
          type="text"
          placeholder="할 일을 입력하세요..."
          class="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
        />
        <button
          type="submit"
          :disabled="!newTodoText.trim()"
          class="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
        >
          추가
        </button>
      </form>
    </div>

    <!-- Todo 항목 리스트 -->
    <div class="flex-1 overflow-y-auto scrollbar-thin">
      <ul class="space-y-3">
        <li
          v-for="todo in dashboardStore.todos"
          :key="todo.id"
          class="py-3 border-b border-gray-100 last:border-b-0"
        >
          <div class="flex items-center gap-3">
            <!-- 체크박스와 텍스트를 label로 감싸기 -->
            <label class="flex items-center gap-3 cursor-pointer flex-1">
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

            <!-- 삭제 버튼 -->
            <button
              @click="handleDeleteTodo(todo.id)"
              class="text-red-500 hover:text-red-700 hover:bg-red-50 p-2 rounded transition-colors"
              title="삭제"
              aria-label="할 일 삭제"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clip-rule="evenodd" />
              </svg>
            </button>
          </div>
        </li>
      </ul>

      <!-- Todo 항목이 없을 때 -->
      <div v-if="dashboardStore.todos.length === 0" class="text-center py-8 text-gray-500">
        <p>할 일이 없습니다</p>
        <p class="text-sm mt-2">위에서 새로운 할 일을 추가해보세요!</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useDashboardStore } from '@/stores/dashboard'

const dashboardStore = useDashboardStore()
const newTodoText = ref('')

// Todo 체크 토글
const handleToggle = (id) => {
  dashboardStore.toggleTodo(id)
}

// Todo 추가
const handleAddTodo = () => {
  if (newTodoText.value.trim()) {
    dashboardStore.addTodo(newTodoText.value.trim())
    newTodoText.value = ''
  }
}

// Todo 삭제
const handleDeleteTodo = (id) => {
  dashboardStore.deleteTodo(id)
}
</script>

<style scoped>
/* TodoList 전용 스타일 */
</style>
