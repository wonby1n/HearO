<template>
  <ClientInfoForm
    :product-name="productName"
    :model-number="modelNumber"
  />
</template>

<script setup>
import { ref } from 'vue'
import ClientInfoForm from '@/components/client/ClientInfoForm.vue'

// 제품 정보 (실제로는 이전 단계에서 받아오거나 자동 인식)
const productName = ref('Galaxy S24 Ultra')
const modelNumber = ref('SM-S928N')
</script>
