<template>
  <div class="todo-card card h-full flex flex-col p-6 bg-white rounded-2xl shadow-sm">
    <div class="flex items-center justify-between mb-6">
      <h3 class="text-lg font-semibold text-gray-800">Todo List</h3>
      <!-- [수정] 문구를 한국어로 변경 -->
      <span class="todo-badge px-3 py-1 rounded-full text-xs font-bold">
        남은 할 일 {{ remainingCount }}개
      </span>
    </div>

    <!-- Todo 추가 입력창 -->
    <div class="add-section mb-6">
      <form @submit.prevent="handleAddTodo" class="flex gap-2">
        <input
          v-model="newTodoText"
          type="text"
          placeholder="할 일을 입력하세요..."
          class="todo-input flex-1 px-4 py-2 border rounded-lg outline-none transition-all"
          :disabled="isLoading"
        />
        <button
          type="submit"
          :disabled="!newTodoText.trim() || isLoading"
          class="todo-add-btn px-5 py-2 text-white font-bold rounded-lg transition-all"
        >
          추가
        </button>
      </form>
    </div>

    <!-- Todo 항목 리스트 -->
    <div class="todo-list-container flex-1 overflow-y-auto pr-2 custom-scrollbar">
      <!-- [수정] 정렬된 리스트(sortedTodos)를 사용 -->
      <ul v-if="sortedTodos.length > 0" class="space-y-2">
        <li
          v-for="todo in sortedTodos"
          :key="todo.id"
          class="todo-item p-4 rounded-xl border border-gray-50 transition-all flex items-center justify-between group"
          :class="{ 'is-completed': todo.completed }"
        >
          <div class="flex items-center gap-4 flex-1">
            <!-- 체크박스 커스텀 -->
            <label class="checkbox-container">
              <input
                type="checkbox"
                :checked="todo.completed"
                @change="handleToggle(todo.id)"
                class="hidden-checkbox"
              />
              <span class="custom-checkbox"></span>
            </label>

            <!-- Todo 텍스트 -->
            <span class="todo-text flex-1 text-sm font-medium transition-all">
              {{ todo.text }}
            </span>
          </div>

          <!-- 삭제 버튼 -->
          <button
            @click="handleDeleteTodo(todo.id)"
            class="delete-btn opacity-0 group-hover:opacity-100 p-2 rounded-lg transition-all"
            title="삭제"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clip-rule="evenodd" />
            </svg>
          </button>
        </li>
      </ul>

      <!-- 빈 상태 표시 -->
      <div v-else class="empty-state flex flex-col items-center justify-center h-full text-center">
        <div class="empty-icon-bg mb-4">
          <svg class="w-12 h-12 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
        </div>
        <p class="text-gray-500 font-semibold">할 일이 없습니다</p>
        <p class="text-xs text-gray-400 mt-1">새로운 목표를 추가해 보세요!</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useDashboardStore } from '@/stores/dashboard'

const dashboardStore = useDashboardStore()
const newTodoText = ref('')
const isLoading = ref(false)

// [추가] 완료되지 않은 할 일은 위로, 완료된 할 일은 아래로 정렬하는 로직
const sortedTodos = computed(() => {
  return [...dashboardStore.todos].sort((a, b) => {
    if (a.completed === b.completed) return 0
    return a.completed ? 1 : -1 // false(미완료)가 먼저 오도록 함
  })
})

// 남은 할 일 개수
const remainingCount = computed(() => {
  return dashboardStore.todos.filter(t => !t.completed).length
})

const handleToggle = (id) => {
  dashboardStore.toggleTodo(id)
}

const handleAddTodo = async () => {
  if (newTodoText.value.trim()) {
    isLoading.value = true
    try {
      await dashboardStore.addTodo(newTodoText.value.trim())
      newTodoText.value = ''
    } finally {
      isLoading.value = false
    }
  }
}

const handleDeleteTodo = (id) => {
  dashboardStore.deleteTodo(id)
}
</script>

<style scoped>
/**
 * [피드백 반영] 테마 및 상태 컬러 CSS 변수화
 */
.todo-card {
  --todo-primary: #1F3A8C;
  --todo-primary-hover: #162a65;
  --todo-border: #e5e7eb;
  --todo-bg-soft: #f9fafb;
  --todo-danger: #ef4444;
  --todo-danger-bg: #fee2e2;
  --todo-success-bg: #E0E7FF;
  
  height: 600px; /* DashboardView의 그리드 높이에 맞춤 */
}

/* 입력창 스타일 */
.todo-input {
  border-color: var(--todo-border);
  background-color: var(--todo-bg-soft);
}

.todo-input:focus {
  border-color: var(--todo-primary);
  box-shadow: 0 0 0 3px rgba(31, 58, 140, 0.1);
}

/* 추가 버튼 스타일 */
.todo-add-btn {
  background-color: var(--todo-primary);
  font-size: 12px;
}

.todo-add-btn:hover:not(:disabled) {
  background-color: var(--todo-primary-hover);
  transform: translateY(-1px);
}

.todo-add-btn:disabled {
  background-color: #d1d5db;
  cursor: not-allowed;
}

/* 뱃지 스타일 */
.todo-badge {
  background-color: var(--todo-success-bg);
  color: var(--todo-primary);
}

/* 리스트 항목 스타일 */
.todo-item {
  background-color: white;
}

.todo-item:hover {
  background-color: var(--todo-bg-soft);
  border-color: var(--todo-primary);
}

.todo-item.is-completed {
  background-color: var(--todo-bg-soft);
}

.todo-item.is-completed .todo-text {
  text-decoration: line-through;
  color: #9ca3af;
}

/* 커스텀 체크박스 */
.checkbox-container {
  position: relative;
  display: flex;
  align-items: center;
  cursor: pointer;
}

.hidden-checkbox {
  display: none;
}

.custom-checkbox {
  width: 22px;
  height: 22px;
  border: 2px solid var(--todo-border);
  border-radius: 6px;
  transition: all 0.2s;
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hidden-checkbox:checked + .custom-checkbox {
  background-color: var(--todo-primary);
  border-color: var(--todo-primary);
}

.custom-checkbox::after {
  content: '✓';
  color: white;
  font-size: 14px;
  font-weight: bold;
  display: none;
}

.hidden-checkbox:checked + .custom-checkbox::after {
  display: block;
}

/* 삭제 버튼 스타일 */
.delete-btn {
  color: var(--todo-text-muted, #9ca3af);
}

.delete-btn:hover {
  color: var(--todo-danger);
  background-color: var(--todo-danger-bg);
}

/* 빈 상태 스타일 */
.empty-icon-bg {
  padding: 1.5rem;
  background-color: var(--todo-bg-soft);
  border-radius: 50%;
}

/* 커스텀 스크롤바 */
.custom-scrollbar::-webkit-scrollbar {
  width: 5px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #e5e7eb;
  border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #d1d5db;
}
</style>