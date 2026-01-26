<script setup>
import { RouterView } from 'vue-router'
import NotificationContainer from '@/components/notification/NotificationContainer.vue'
</script>

<template>
  <RouterView />
  <NotificationContainer position="top-right" />
</template>

<style scoped>
/* App 레벨 스타일 */
</style>
