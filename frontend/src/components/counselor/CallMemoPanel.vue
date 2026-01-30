<template>
  <section>
    <div class="flex items-center justify-between mb-3">
      <h4 class="text-sm font-semibold text-gray-900">메모</h4>
      <span v-if="savedLabel" class="text-xs text-gray-500">{{ savedLabel }}</span>
    </div>
    <textarea
      :value="modelValue"
      :placeholder="placeholder"
      class="w-full h-32 px-3 py-2 border border-gray-300 rounded-lg resize-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 text-sm"
      @input="emit('update:modelValue', $event.target.value)"
    ></textarea>
  </section>
</template>

<script setup>
defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '상담 메모를 입력하세요.'
  },
  savedLabel: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])
</script>
