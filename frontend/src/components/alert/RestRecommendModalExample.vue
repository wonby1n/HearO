<template>
  <div class="example-container">
    <h1>RestRecommendModal 테스트</h1>

    <button class="test-button" @click="showModal = true">
      모달 열기
    </button>

    <RestRecommendModal
      :show="showModal"
      @close="showModal = false"
      @confirm="handleConfirm"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import RestRecommendModal from './RestRecommendModal.vue'

const showModal = ref(false)

const handleConfirm = () => {
  console.log('확인 버튼 클릭됨')
  showModal.value = false
}
</script>

<style scoped>
.example-container {
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  min-height: 100vh;
  background: #1a202c;
}

h1 {
  color: #ffffff;
  font-size: 28px;
  margin: 0;
}

.test-button {
  padding: 14px 32px;
  background: #1F3A8C;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(31, 58, 140, 0.3);
}

.test-button:hover {
  background: #1a3278;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(31, 58, 140, 0.4);
}

.test-button:active {
  transform: translateY(0);
}
</style>
